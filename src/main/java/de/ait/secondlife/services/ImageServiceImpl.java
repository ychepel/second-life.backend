package de.ait.secondlife.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import de.ait.secondlife.constants.EntityType;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.dto.ImageCreationDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.entity.ImageEntity;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.*;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.ParameterIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.ImagesNotFoundException;
import de.ait.secondlife.repositories.ImageRepository;
import de.ait.secondlife.services.interfaces.ImageService;
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

    //TODO: PR review - move these count values to enum EntityType
    private final int MAX_IMAGE_COUNT_FOR_OFFER = 5;
    private final int MAX_IMAGE_COUNT = 1;

    @Override
    public void saveNewImage(ImageCreationDto dto) {
        MultipartFile file = dto.getFile();
        checkFile(file);

        String entityType = EntityType.get(dto.getEntityType()).getType();
        Long entityId = dto.getEntityId();

        //TODO: PR review - add checking for existing entity with requested ID and throw exception if it is not existed

        //TODO: add checking authority rights for attaching images to a requested entity

        ImagePathsResponseDto currentImages = findAllImageForEntity(entityType, entityId);

        checkCountOfImageForEntityType(currentImages, entityType);

        ObjectMetadata metadata = createMetadata(file);

        Set<int[]> fileSizes = getFileSizesForEntityType(entityType);

        Path path = Path.of(dirPrefix, entityType, entityId.toString());
        UUID baseName = UUID.randomUUID();

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

            repository.save(ImageEntity.builder()
                    .size(size)
                    .baseName(baseName.toString())
                    .entityType(entityType)
                    .entityId(entityId)
                    .fullPath(toUnixStylePath(basePath + imgPath))
                    .build());
        });
    }

    @Override
    public ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId) {

        Set<ImageEntity> imgs = repository.findAllByEntityIdAndEntityType(entityId, entityType);

        Map<Integer, Map<String, String>> images = new HashMap<>();

        if (!imgs.isEmpty()) {
            Set<String> imgBaseNames = new HashSet<>();
            imgs.forEach(e -> imgBaseNames.add(e.getBaseName()));

            Integer i = 0;
            for (String img : imgBaseNames) {
                Map<String, String> files = images.computeIfAbsent(i, k -> new HashMap<>());
                Set<ImageEntity> imgsByBaseName = imgs.stream()
                        .filter(e -> e.getBaseName().equals(img))
                        .collect(Collectors.toSet());
                imgsByBaseName.forEach(e -> files.putIfAbsent(e.getSize(), e.getFullPath()));
                i++;
            }
        }
        return new ImagePathsResponseDto(images);
    }

    @Transactional
    @Override
    public void deleteImage(String fileNme) {
//TODO   Only the owner and admin has the right to make changes
        String baseName = getBaseNameOfImage(fileNme);

        Set<ImageEntity> images = repository.findAllByBaseName(baseName);
        if (images.isEmpty()) throw new ImagesNotFoundException(baseName);

        images.forEach(e -> {
            String doFileName = e.getFullPath().substring(basePath.length());
            s3Client.deleteObject(bucketName, doFileName);
        });

        repository.deleteAllByBaseName(baseName);
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

    private void checkCountOfImageForEntityType(ImagePathsResponseDto currentImages, String entityType) {
        if (entityType.equals(EntityType.OFFER.getType())) {
            if (currentImages.getImages().size() >= MAX_IMAGE_COUNT_FOR_OFFER)
                throw new MaxImageCountException(MAX_IMAGE_COUNT_FOR_OFFER);
        } else {
            if (currentImages.getImages().size() >= MAX_IMAGE_COUNT)
                throw new MaxImageCountException(MAX_IMAGE_COUNT);
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
        return switch (EntityType.get(entityType)) {
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
