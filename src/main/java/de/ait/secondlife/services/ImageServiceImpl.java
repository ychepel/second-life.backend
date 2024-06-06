package de.ait.secondlife.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import de.ait.secondlife.constants.EntityType;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.dto.ImageCreateDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.entity.EntityImage;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileFormatException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileSizeException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.ParameterIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.FileNotFoundExecption;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.ImagesNotFoundException;
import de.ait.secondlife.repositories.ImageRepository;
import de.ait.secondlife.services.interfaces.ImageService;
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

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService, ImageConstants {

    private final AmazonS3 s3Client;
    private final ImageRepository repository;

    @Value("${BUCKET_NAME}")
    private String bucketName;

    @Value("${DIR_PREFIX}")
    private String dirPrefix;

    @Override
    public void saveNewImage(ImageCreateDto dto) {
        MultipartFile file = dto.getFile();
        String entityType = EntityType.get(dto.getEntityCode()).getType();
        Long entityId = dto.getEntityId();
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

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    toUnixStylePath(Path.of(path.toString(), makeFileName(size, baseName.toString()))),
                    compressFile(file, e),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicReadWrite);
            s3Client.putObject(request);

            repository.save(EntityImage.builder()
                    .extension(IMG_EXP)
                    .size(size)
                    .baseName(baseName.toString())
                    .path(toUnixStylePath(path))
                    .entityType(entityType)
                    .entityId(entityId)
                    .build());
        });
    }

    @Override
    public ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId) {

        Set<EntityImage> imgs = repository.findAllByEntityIdAndEntityType(entityId, entityType);
        if (imgs.isEmpty()) throw new ImagesNotFoundException(entityType, entityId);
        Path path = Path.of(dirPrefix, entityType, entityId.toString());

        Map<String, String> map = new HashMap<>();
        imgs.forEach(
                e -> map.put(e.getSize(),
                        toUnixStylePath(Path.of(
                                path.toString(),
                                makeFileName(e.getSize(),
                                        e.getBaseName()))
                        )));
        return new ImagePathsResponseDto(map);
    }

    @Override
    public InputStream getImage(String fileName) {
        if(fileName.isBlank()) throw new FileNotFoundExecption(fileName);
        S3Object s3Object;
        try {
            s3Object = s3Client.getObject(bucketName, fileName);
        }catch (Exception e) {
            throw new FileNotFoundExecption(fileName);
        }
        return s3Object.getObjectContent();
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
