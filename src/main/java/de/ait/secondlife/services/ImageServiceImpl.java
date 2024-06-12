package de.ait.secondlife.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import de.ait.secondlife.constants.EntityTypeWithImgs;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.dto.ImageCreationDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.entity.ImageEntity;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.*;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.ImagesNotFoundException;
import de.ait.secondlife.repositories.ImageRepository;
import de.ait.secondlife.services.interfaces.ImageService;
import de.ait.secondlife.services.utilities.EntityUtilities;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService, ImageConstants {

    private final AmazonS3 s3Client;
    private final ImageRepository repository;


    @Value("${do.buket.name}")
    private String bucketName;

    @Value("${do.dir.prefix}")
    private String dirPrefix;

    @Value("${do.base.path}")
    private String basePath;

    @Override
    public ImagePathsResponseDto saveNewImage(String entityType, Long entityId, ImageCreationDto dto) {
        MultipartFile file = dto.getFile();
        checkFile(file);

        //TODO: add checking authority rights for attaching images to a requested entity

        ImagePathsResponseDto currentImages = findAllImageForEntity(entityType, entityId);

        checkCountOfImageForEntityType(currentImages, entityType);

        ObjectMetadata metadata = createMetadata(file);

        Set<int[]> fileSizes = getFileSizesForEntityType(entityType);

        UUID baseName = UUID.randomUUID();
        Path path = entityId != null ?
                Path.of(dirPrefix, entityType, entityId.toString()) :
                Path.of(TEMP_IMG_DIR, baseName.toString());
        Set<ImageEntity> savedImgEntities = new HashSet<>();
        fileSizes.forEach(e -> {
            String size = e[0] + "x" + e[1];

            Path pathForDo = Path.of(path.toString(), makeFileName(size, baseName.toString()));

            String imgPath = toUnixStylePath(pathForDo.toString());

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    imgPath,
                    compressFile(file, e),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicReadWrite);

            s3Client.putObject(request);

            ImageEntity savedImgEntity = repository.save(ImageEntity.builder()
                    .size(size)
                    .baseName(baseName.toString())
                    .entityType(entityType)
                    .entityId(entityId)
                    .fullPath(toUnixStylePath(basePath + imgPath))
                    .build());
            savedImgEntities.add(savedImgEntity);
        });
        return getImagePathsResponseDto(savedImgEntities);
    }

    @Override
    public ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId) {
        Set<ImageEntity> imgs = repository.findAllByEntityIdAndEntityType(entityId, entityType);
        return getImagePathsResponseDto(imgs);
    }

    @Override
    public void connectTempImgsToEntity(Set<String> baseNames, Long entityId) {
        if (baseNames != null && entityId != null) {
            baseNames.forEach(e -> {
                        Set<ImageEntity> imgs = repository.findAllByBaseName(e);

                        imgs.forEach(k -> {
                            k.setEntityId(entityId);
                            Path path = Path.of(dirPrefix,
                                    k.getEntityType(),
                                    entityId.toString(),
                                    makeFileName(k.getSize(), e));

                            String oldPath = k.getFullPath();
                            String newPath = toUnixStylePath(basePath + path);

                            k.setFullPath(newPath);
                            repository.save(k);
                            relocateImgFile(oldPath, newPath);
                        });
                    }
            );
        }
    }

    private void relocateImgFile(String oldPath, String newPath) {
        String oldDoPath = oldPath.substring(basePath.length());
        String newDoPath = newPath.substring(basePath.length());
        s3Client.copyObject(
                bucketName,
                oldDoPath,
                bucketName,
                newDoPath
        );
        s3Client.deleteObject(bucketName, oldDoPath);
    }

    @Transactional
    @Override
    public void deleteImage(String baseName) {
//TODO   Only the owner and admin has the right to make changes

        Set<ImageEntity> images = repository.findAllByBaseName(baseName);
        if (images.isEmpty()) throw new ImagesNotFoundException(baseName);

        images.forEach(e -> {
            String doFileName = e.getFullPath().substring(basePath.length());
            s3Client.deleteObject(bucketName, doFileName);
        });

        repository.deleteAllByBaseName(baseName);
    }

    private ImagePathsResponseDto getImagePathsResponseDto(Set<ImageEntity> imgs) {
        Map<String, Map<String, String>> images = new HashMap<>();

        if (!imgs.isEmpty()) {
            Set<String> imgBaseNames = new HashSet<>();

            imgs.forEach(e -> imgBaseNames.add(e.getBaseName()));

            for (String baseName : imgBaseNames) {
                Map<String, String> files = images.computeIfAbsent(baseName, k -> new HashMap<>());

                Set<ImageEntity> imgsByBaseName = imgs.stream()
                        .filter(e -> e.getBaseName().equals(baseName))
                        .collect(Collectors.toSet());
                imgsByBaseName.forEach(e -> files.putIfAbsent(e.getSize(), e.getFullPath()));
            }
        }
        return new ImagePathsResponseDto(images);
    }


    private InputStream compressFile(MultipartFile file, int[] size) {

        ByteArrayOutputStream outputStream;
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();

            double aspectRatio = (double) originalWidth / originalHeight;
            int newWidth, newHeight;

            if (originalWidth > originalHeight) {
                newWidth = size[0];
                newHeight = (int) (size[0] / aspectRatio);
            } else {
                newHeight = size[1];
                newWidth = (int) (size[1] * aspectRatio);
            }

            BufferedImage resizedImage = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, size[0], size[1]);

            int x = (size[0] - newWidth) / 2;
            int y = (size[1] - newHeight) / 2;
            g2d.drawImage(image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), x, y, null);
            g2d.dispose();

            outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, IMG_EXP, outputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void checkFile(MultipartFile file) {
        if (file.isEmpty())
            throw new BadRequestException(String.format("File <%s> is empty", file.getOriginalFilename()));
        if (file.getSize() > MAX_FILE_SIZE)
            throw new BadFileSizeException(file.getOriginalFilename(), file.getSize(), MAX_FILE_SIZE);
    }

    private void checkCountOfImageForEntityType(
            ImagePathsResponseDto currentImages,
            String entityType) {
        int maxCountOfImage = EntityTypeWithImgs.get(entityType.toLowerCase()).getMaxCountOfImgs();
        if (currentImages.getValues().size() >= maxCountOfImage) {
            throw new MaxImageCountException(entityType, maxCountOfImage);
        }
    }

    private ObjectMetadata createMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();

        if (file != null && file.getContentType() != null) {
            String type = file.getContentType();
            if (!type.startsWith("image")) throw new BadFileFormatException();
            metadata.setContentType(type);
        } else throw new BadFileFormatException();

        return metadata;
    }

    private Set<int[]> getFileSizesForEntityType(String entityType) {
        return switch (EntityTypeWithImgs.get(entityType.toLowerCase())) {
            case OFFER -> Set.of(IMG_1_SIZE, IMG_2_SIZE, IMG_3_SIZE);
            case USER -> Set.of(IMG_2_SIZE, IMG_3_SIZE);
            case CATEGORY -> Set.of(IMG_3_SIZE);
        };
    }

    private String getBaseNameOfImage(String fileNme) {
        int startIndex = fileNme.indexOf('_') + 1;
        int endIndex = fileNme.lastIndexOf('.');
        if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex)
            return fileNme.substring(startIndex, endIndex);
        else throw new FileNameIsWrongException(fileNme);
    }

    private String toUnixStylePath(String path) {
        return path.replace("\\", "/");
    }

    private String makeFileName(String size, String baseName) {
        return String.format("%s_%s.%s", size, baseName, IMG_EXP);
    }
}
