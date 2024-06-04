package de.ait.secondlife.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.entity.EntityImage;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileFormatException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileSizeException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.ParameterIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;
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
import java.util.Set;
import java.util.UUID;

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
    public void saveNewImage(MultipartFile file, String entityType, Long entityId) {

        ObjectMetadata metadata = new ObjectMetadata();

        if (file != null && file.getContentType() != null) {
            String type = file.getContentType();
            if (!type.startsWith("image")) throw new BadFileFormatException();
            metadata.setContentType(type);
        } else throw new BadFileFormatException();

        Set<int[]> fileSizes = switch (entityType) {
            case OFFER -> Set.of(IMG_1_SIZE, IMG_2_SIZE, IMG_3_SIZE);
            case USER -> Set.of(IMG_2_SIZE, IMG_3_SIZE);
            case CATEGORY -> Set.of(IMG_3_SIZE);
            default -> throw new BadEntityTypeException(entityType);
        };
        Path path = Path.of(dirPrefix, entityType, entityId.toString());

        EntityImage newImage = EntityImage.builder()
                .extension(IMG_EXP)
                .path(path.toString())
                .entityType(entityType)
                .entityId(entityId)
                .build();

        fileSizes.forEach(e -> {
            UUID baseName = UUID.randomUUID();
            String size = e[0] + "x" + e[1];
            String fileName = String.format("%s_%s.%s", size, baseName, IMG_EXP);

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    Path.of(path.toString(), fileName).toString(),
                    compressFile(file, e),
                    metadata
            ).withCannedAcl(CannedAccessControlList.Private);
            s3Client.putObject(request);

            newImage.setBaseName(baseName.toString());
            newImage.setSize(size);

            repository.save(newImage);
        });
    }

    @Override
    public Set<String> findAllImageForEntity(String entityType, Long entityId) {
        return Set.of();
    }

    @Override
    public InputStream getImage(String size, String baseName) {
        return null;
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

}
