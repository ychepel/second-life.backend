package de.ait.secondlife.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.dto.ImageCreationDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.entity.ImageEntity;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileFormatException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadFileSizeException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadRequestException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.MaxImageCountException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.ImagesNotFoundException;
import de.ait.secondlife.repositories.ImageRepository;
import de.ait.secondlife.services.interfaces.*;
import de.ait.secondlife.services.utilities.UserPermissionsUtilities;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.security.auth.login.CredentialException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService, ImageConstants {

    private final AmazonS3 s3Client;
    private final ImageRepository repository;
    @Lazy
    @Autowired
    private OfferService offerService;
    @Lazy
    @Autowired
    private UserService userService;
    @Lazy
    @Autowired
    private CategoryService categoryService;
    private final UserPermissionsUtilities userCredentialsUtilities;

    @Value("${do.buket.name}")
    private String bucketName;

    @Value("${do.dir.prefix}")
    private String dirPrefix;

    @Value("${do.base.path}")
    private String basePath;

    @Override
    @Transactional
    public ImagePathsResponseDto saveNewImage(String entityType, Long entityId, ImageCreationDto dto) {

        checkEntityExists(entityType, entityId);
        Long userId = -1L;
        try {
            userId = userService.getCurrentUser().getId();
        } catch (CredentialException ignored) {
        }
        if (entityId != null) userCredentialsUtilities.checkUserPermissions(entityType, entityId);

        MultipartFile file = dto.getFile();
        checkFile(file);

        ImagePathsResponseDto currentImages = findAllImageForEntity(entityType, entityId);

        if (entityId != null) checkCountOfImageForEntityType(currentImages, entityType);

        ObjectMetadata metadata = createMetadata(file);

        Set<int[]> fileSizes = getFileSizesForEntityType(entityType);

        UUID baseName = UUID.randomUUID();
        Path path = entityId != null ?
                Path.of(dirPrefix, entityType, entityId.toString()) :
                Path.of(TEMP_IMAGE_DIR, userId.toString(), baseName.toString());
        Set<ImageEntity> savedImgEntities = new HashSet<>();
        fileSizes.forEach(e -> {
            String size = e[0] + "x" + e[1];

            Path pathForDo = Path.of(path.toString(), makeFileName(size, baseName.toString()));

            String imagePath = toUnixStylePath(pathForDo.toString());

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    imagePath,
                    compressFile(file, e),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicReadWrite);

            s3Client.putObject(request);

            LocalDateTime now = LocalDateTime.now();
            ImageEntity savedImgEntity = repository.save(
                    ImageEntity.builder()
                            .size(size)
                            .baseName(baseName.toString())
                            .entityType(entityType)
                            .entityId(entityId)
                            .fullPath(toUnixStylePath(basePath + imagePath))
                            .updatedAt(now)
                            .build()
            );
            if (entityId == null) savedImgEntity.setCreatedAt(now);

            savedImgEntities.add(savedImgEntity);
        });
        return getImagePathsResponseDto(savedImgEntities);
    }

    @Override
    public ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId) {
        Set<ImageEntity> images = repository.findAllByEntityIdAndEntityType(entityId, entityType);
        return getImagePathsResponseDto(images);
    }

    @Override
    public void connectTempImagesToEntity(Set<String> baseNames, String entityType, Long entityId) {
        if (baseNames != null && entityId != null) {
            Set<String> usedBaseNames = new HashSet<>();
            baseNames.forEach(e -> {
                        Set<ImageEntity> images = findAllImagesByBaseName(e);

                        images.forEach(k -> {
                            if (k.getEntityId() == null && k.getEntityType().equals(entityType)) {

                                k.setEntityId(entityId);
                                Path path = Path.of(dirPrefix,
                                        k.getEntityType(),
                                        entityId.toString(),
                                        makeFileName(k.getSize(), e));

                                String oldPath = k.getFullPath();
                                String newPath = toUnixStylePath(basePath + path);

                                k.setFullPath(newPath);
                                k.setUpdatedAt(LocalDateTime.now());
                                repository.save(k);
                                relocateImageFile(oldPath, newPath);
                            } else usedBaseNames.add(k.getBaseName());
                        });
                    }
            );
            if (!usedBaseNames.isEmpty())
                log.warn("Images with base names <{}> were not uploaded as they had been used previously " +
                                "or the type of entity is wrong",
                        String.join(", ", usedBaseNames));
        }
    }

    @Transactional
    @Override
    public void deleteImage(String baseName) {

        Set<ImageEntity> images = findAllImagesByBaseName(baseName);
        if (images.isEmpty()) throw new ImagesNotFoundException(baseName);

        images.forEach(e -> {
            String doFileName = e.getFullPath().substring(basePath.length());
            s3Client.deleteObject(bucketName, doFileName);
        });
        repository.deleteAllByBaseName(baseName);
    }

    @Override
    public Set<ImageEntity> findAllImagesByBaseName(String baseName) {
        return repository.findAllByBaseName(baseName);
    }

    private void relocateImageFile(String oldPath, String newPath) {
        String oldDoPath = oldPath.substring(basePath.length());
        String newDoPath = newPath.substring(basePath.length());
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(
                bucketName,
                oldDoPath,
                bucketName,
                newDoPath
        ).withCannedAccessControlList(CannedAccessControlList.PublicReadWrite);
        s3Client.copyObject(copyObjRequest);
        s3Client.deleteObject(bucketName, oldDoPath);
    }

    private ImagePathsResponseDto getImagePathsResponseDto(Set<ImageEntity> imagesSet) {
        Map<String, Map<String, String>> images = new HashMap<>();

        if (!imagesSet.isEmpty()) {
            Set<String> imageBaseNames = new HashSet<>();

            imagesSet.forEach(e -> imageBaseNames.add(e.getBaseName()));

            for (String baseName : imageBaseNames) {
                Map<String, String> files = images.computeIfAbsent(baseName, k -> new HashMap<>());

                Set<ImageEntity> imagesByBaseName = imagesSet.stream()
                        .filter(e -> e.getBaseName().equals(baseName))
                        .collect(Collectors.toSet());
                imagesByBaseName.forEach(e -> files.putIfAbsent(e.getSize(), e.getFullPath()));
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
            ImageIO.write(resizedImage, IMAGE, outputStream);
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
        int maxCountOfImage = EntityTypeWithImages.get(entityType.toLowerCase()).getMaxCountOfImages();
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
        return switch (EntityTypeWithImages.get(entityType.toLowerCase())) {
            case OFFER -> Set.of(IMAGE_1_SIZE, IMAGE_2_SIZE, IMAGE_3_SIZE);
            case USER -> Set.of(IMAGE_2_SIZE, IMAGE_3_SIZE);
            case CATEGORY -> Set.of(IMAGE_2_SIZE);
        };
    }

    private String toUnixStylePath(String path) {
        return path.replace("\\", "/");
    }

    private String makeFileName(String size, String baseName) {
        return String.format("%s_%s.%s", size, baseName, IMAGE);
    }

    public void checkEntityExists(String entityType, Long entityId) {
        if (entityId == null) return;
        CheckEntityExistsService service;
        switch (EntityTypeWithImages.get(entityType.toLowerCase())) {
            case OFFER -> service = offerService;
            case USER -> service = userService;
            case CATEGORY -> service = categoryService;
            default -> throw new BadEntityTypeException(entityType);
        }
        if (!service.checkEntityExistsById(entityId)) throw new BadEntityTypeException(entityType, entityId);
    }
}
