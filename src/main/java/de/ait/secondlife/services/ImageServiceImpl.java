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
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the ImageService interface.(Version 1.0)
 * This service provides methods to handle image operations such as saving, deleting, and connecting images to entities.
 * It interacts with AWS S3 for storing and managing images and with various repositories for storing image metadata.
 *
 * <p>
 * This class utilizes services for different entities (offers, users, categories) to verify existence and permissions
 * before performing operations on images related to those entities.
 * </p>
 *
 * <p>
 * The class uses Amazon S3 for storing images and various utilities for permission checking and metadata creation.
 * </p>
 *
 * <p>
 * Note: This class requires the following configuration properties:
 * - do.buket.name: The name of the S3 bucket.
 * - do.dir.prefix: The directory prefix for storing images.
 * - do.base.path: The base path for accessing images.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link BadRequestException} - if the provided file is empty</li>
 *     <li>{@link BadFileSizeException} - if the file size exceeds the maximum allowed size</li>
 *     <li>{@link BadFileFormatException} - if the file format is not supported</li>
 *     <li>{@link MaxImageCountException} - if the number of images for the entity type exceeds the allowed limit</li>
 *     <li>{@link ImagesNotFoundException} - if no images are found for the given base name</li>
 *     <li>{@link BadEntityTypeException} - if the entity type is not recognized or the entity does not exist</li>
 *     <li>{@link CredentialException} - if there is an issue with user credentials</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @version 1.0
 * @author: Second Life Team
 */
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

    /**
     * Saves a new image for a given entity type and entity ID.
     * The image is validated, compressed, and uploaded to S3.
     *
     * @param entityType the type of the entity (e.g., "offer", "user", "category")
     * @param entityId   the ID of the entity
     * @param dto        the image creation DTO containing the image file
     * @return ImagePathsResponseDto containing paths of the saved images
     * @throws BadRequestException    if the provided file is empty
     * @throws BadFileSizeException   if the file size exceeds the maximum allowed size
     * @throws BadFileFormatException if the file format is not supported
     * @throws MaxImageCountException if the number of images for the entity type exceeds the allowed limit
     * @throws BadEntityTypeException if the entity type is not recognized or the entity does not exist
     */
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

    /**
     * Finds all images for a given entity type and entity ID.
     *
     * @param entityType the type of the entity
     * @param entityId   the ID of the entity
     * @return ImagePathsResponseDto containing paths of the images
     * @throws BadEntityTypeException if the entity type is not recognized or the entity does not exist
     */
    @Override
    public ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId) {
        Set<ImageEntity> images = repository.findAllByEntityIdAndEntityType(entityId, entityType);
        return getImagePathsResponseDto(images);
    }

    /**
     * Connects temporary images to a given entity.
     * This method updates the entity ID and paths of the images.
     *
     * @param baseNames  the set of base names of the images
     * @param entityType the type of the entity
     * @param entityId   the ID of the entity
     * @throws BadEntityTypeException if the entity type is not recognized or the entity does not exist
     */
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

    /**
     * Deletes an image by its base name.
     * This method removes the image from both S3 and the repository.
     *
     * @param baseName the base name of the image
     * @throws ImagesNotFoundException if no images are found for the given base name
     * @throws CredentialException     if there is an issue with user credentials
     */
    @Transactional
    @Override
    public void deleteImage(String baseName) {

        Set<ImageEntity> images = findAllImagesByBaseName(baseName);
        if (images.isEmpty()) throw new ImagesNotFoundException(baseName);

        userCredentialsUtilities.checkUserPermissionsForImageByImageEntities(images);

        images.forEach(e -> {
            String doFileName = e.getFullPath().substring(basePath.length());
            s3Client.deleteObject(bucketName, doFileName);
        });
        repository.deleteAllByBaseName(baseName);
    }

    /**
     * Finds all images by their base name.
     *
     * @param baseName the base name of the images
     * @return the set of ImageEntity objects
     */
    @Override
    public Set<ImageEntity> findAllImagesByBaseName(String baseName) {
        return repository.findAllByBaseName(baseName);
    }

    /**
     * Relocates an image file from one path to another in S3.
     *
     * @param oldPath the old path of the image
     * @param newPath the new path of the image
     */
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

    /**
     * Deletes unattached images that are older than a specified date.
     * This method is transactional to ensure atomicity of the delete operation.
     */
    @Override
    @Transactional
    public void deleteUnattachedImages() {
        LocalDateTime obsoleteDate = LocalDateTime.now().minusDays(1);
        List<ImageEntity> imagesForDelete = repository.findAllByEntityIdIsNullAndCreatedAtLessThan(obsoleteDate);
        Set<String> baseNames = new HashSet<>();
        imagesForDelete.forEach(image -> {
            String fileName = image.getFullPath().substring(basePath.length());
            s3Client.deleteObject(bucketName, fileName);
            baseNames.add(image.getBaseName());
        });
        repository.deleteAllByBaseNameIn(baseNames);
    }

    /**
     * Creates an ImagePathsResponseDto from a set of ImageEntity objects.
     *
     * @param imagesSet the set of ImageEntity objects
     * @return ImagePathsResponseDto containing the paths of the images
     */
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

    /**
     * Compresses an image file to a specified size.
     *
     * @param file the image file to compress
     * @param size the target size for the compressed image
     * @return InputStream of the compressed image
     * @throws RuntimeException if an IOException occurs during image processing
     */
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

    /**
     * Checks if the provided file is valid.
     *
     * @param file the image file to check
     * @throws BadRequestException  if the provided file is empty
     * @throws BadFileSizeException if the file size exceeds the maximum allowed size
     */
    private void checkFile(MultipartFile file) {
        if (file.isEmpty())
            throw new BadRequestException(String.format("File <%s> is empty", file.getOriginalFilename()));
        if (file.getSize() > MAX_FILE_SIZE)
            throw new BadFileSizeException(file.getOriginalFilename(), file.getSize(), MAX_FILE_SIZE);
    }

    /**
     * Checks if the number of images for an entity type exceeds the allowed maximum.
     *
     * @param currentImages the current images for the entity
     * @param entityType    the type of the entity
     * @throws MaxImageCountException if the number of images for the entity type exceeds the allowed limit
     */
    private void checkCountOfImageForEntityType(
            ImagePathsResponseDto currentImages,
            String entityType) {
        int maxCountOfImage = EntityTypeWithImages.get(entityType.toLowerCase()).getMaxCountOfImages();
        if (currentImages.getValues().size() >= maxCountOfImage) {
            throw new MaxImageCountException(entityType, maxCountOfImage);
        }
    }

    /**
     * Creates metadata for an image file.
     *
     * @param file the image file
     * @return ObjectMetadata for the image file
     * @throws BadFileFormatException if the file format is not supported
     */
    private ObjectMetadata createMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();

        if (file != null && file.getContentType() != null) {
            String type = file.getContentType();
            if (!type.startsWith("image")) throw new BadFileFormatException();
            metadata.setContentType(type);
        } else throw new BadFileFormatException();

        return metadata;
    }

    /**
     * Gets the file sizes for a given entity type.
     *
     * @param entityType the type of the entity
     * @return Set of int arrays representing the sizes
     */
    private Set<int[]> getFileSizesForEntityType(String entityType) {
        return switch (EntityTypeWithImages.get(entityType.toLowerCase())) {
            case OFFER -> Set.of(IMAGE_1_SIZE, IMAGE_2_SIZE, IMAGE_3_SIZE);
            case USER -> Set.of(IMAGE_2_SIZE, IMAGE_3_SIZE);
            case CATEGORY -> Set.of(IMAGE_2_SIZE);
        };
    }

    /**
     * Converts a file path to Unix style.
     *
     * @param path the original file path
     * @return the Unix style file path
     */
    private String toUnixStylePath(String path) {
        return path.replace("\\", "/");
    }

    /**
     * Creates a file name from a size and base name.
     *
     * @param size     the size of the image
     * @param baseName the base name of the image
     * @return the generated file name
     */
    private String makeFileName(String size, String baseName) {
        return String.format("%s_%s.%s", size, baseName, IMAGE);
    }

    /**
     * Checks if an entity exists by its type and ID.
     *
     * @param entityType the type of the entity
     * @param entityId   the ID of the entity
     * @throws BadEntityTypeException if the entity type is not recognized or the entity does not exist
     */
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
