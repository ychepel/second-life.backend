package de.ait.secondlife.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import de.ait.secondlife.constants.EntityType;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.dto.ImageCreateDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.entity.ImageEntity;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileFormatException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileSizeException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.FileNameIsWrongException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.MaxImageCountException;
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

    private final int MAX_IMAGE_COUNT_FOR_OFFER = 5;
    private final int MAX_IMAGE_COUNT = 1;

    @Override
    public void saveNewImage(ImageCreateDto dto) {
        String entityType = EntityType.get(dto.getEntityType()).getType();
        Long entityId = dto.getEntityId();

        ImagePathsResponseDto currentImages = findAllImageForEntity(entityType, entityId);

        if (entityType.equals(EntityType.OFFER.getType())) {
            if (currentImages.getImages().size() >= MAX_IMAGE_COUNT_FOR_OFFER)
                throw new MaxImageCountException(MAX_IMAGE_COUNT_FOR_OFFER);
        } else {
            if (currentImages.getImages().size() >= MAX_IMAGE_COUNT)
                throw new MaxImageCountException(MAX_IMAGE_COUNT);
        }

        MultipartFile file = dto.getFile();
        ObjectMetadata metadata = new ObjectMetadata();

        if (file != null && file.getContentType() != null) {
            String type = file.getContentType();
            if (!type.startsWith("image")) throw new BadFileFormatException();
            metadata.setContentType(type);
        } else throw new BadFileFormatException();

        Set<int[]> fileSizes =
                switch (EntityType.get(entityType)) {
                    case OFFER -> Set.of(IMG_1_SIZE, IMG_2_SIZE, IMG_3_SIZE);
                    case USER -> Set.of(IMG_2_SIZE, IMG_3_SIZE);
                    case CATEGORY -> Set.of(IMG_3_SIZE);
                };

        Path path = Path.of(dirPrefix, entityType, entityId.toString());
        UUID baseName = UUID.randomUUID();

        fileSizes.forEach(e -> {
            String size = e[0] + "x" + e[1];
            Path pathForDo = Path.of(path.toString(), makeFileName(size, baseName.toString()));

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    toUnixStylePath(pathForDo),
                    compressFile(file, e),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicReadWrite);

            s3Client.putObject(request);

            Path fullPath = Path.of(basePath, pathForDo.toString());

            repository.save(ImageEntity.builder()
                    .size(size)
                    .baseName(baseName.toString())
                    .entityType(entityType)
                    .entityId(entityId)
                    .fullPath(toUnixStylePath(fullPath))
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
        String baseName;
        int startIndex = fileNme.indexOf('_') + 1;
        int endIndex = fileNme.indexOf('.');
        if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex)
            baseName = fileNme.substring(startIndex, endIndex);
        else throw new FileNameIsWrongException(fileNme);

        Set<ImageEntity> images = repository.findAllByBaseName(baseName);
        if (images.isEmpty()) throw new ImagesNotFoundException(baseName);

        Set<String> imagesNames = images.stream()
                .map(e -> {
                            Path path = Path.of(dirPrefix, e.getEntityType(), e.getEntityId().toString());
                            return toUnixStylePath(Path.of(path.toString(), makeFileName(e.getSize(), baseName.toString())));
                        }
                ).collect(Collectors.toSet());

        imagesNames.forEach(e -> s3Client.deleteObject(bucketName, e));

        repository.deleteAllByBaseName(baseName);
    }

    private InputStream compressFile(MultipartFile file, int[] size) {

        if (file == null && file.isEmpty())
            throw new ParameterIsNullException(
                    String.format("File<%s> is null or empty", file.getOriginalFilename()));

        if (file.getSize() > MAX_FILE_SIZE)
            throw new BadFileSizeException(file.getOriginalFilename(), file.getSize(), MAX_FILE_SIZE);

        ByteArrayOutputStream outputStream;
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(image.getScaledInstance(size[0], size[1], Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();

            outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, IMG_EXP, outputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private String toUnixStylePath(Path path) {
        return path.toString().replace("\\", "/");
    }

    private String makeFileName(String size, String baseName) {
        return String.format("%s_%s.%s", size, baseName, IMG_EXP);
    }
}
