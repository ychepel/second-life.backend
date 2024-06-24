package de.ait.secondlife.services.utilities;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.entity.ImageEntity;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.NoRightsException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.PathWrongException;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.ImageService;
import de.ait.secondlife.services.interfaces.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.security.auth.login.CredentialException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for checking user permissions on various entities (Version 1.0).
 * This component provides methods to check user permissions for different entity types
 * such as offers, users, and categories before performing operations on these entities.
 *
 * <p>
 * The class interacts with various services to fetch necessary data for permission checks.
 * It uses lazy loading for dependencies to avoid circular dependencies during bean initialization.
 * </p>
 *
 * <p>
 * Note: This class requires the following configuration properties:
 * - do.base.path: The base path for accessing images.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link NoRightsException} - if the user does not have the required permissions</li>
 *     <li>{@link UserIsNotAuthorizedException} - if the user is not authorized</li>
 *     <li>{@link CredentialException} - if there is an issue with user credentials</li>
 *     <li>{@link PathWrongException} - if the image path is incorrect</li>
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
@Component
public class UserPermissionsUtilities {
    @Lazy
    @Autowired
    private OfferService offerService;
    @Lazy
    @Autowired
    private ImageService imageService;

    @Value("${do.base.path}")
    private String basePath;

    /**
     * Checks if the current user has permission to perform actions on a given user ID.
     *
     * @param userId the ID of the user to check permissions for
     * @throws NoRightsException            if the user does not have enough rights
     * @throws UserIsNotAuthorizedException if the user is not authorized
     * @throws CredentialException          if there is an issue with user credentials
     */
    public void checkUserPermissions(Long userId) {
        Role role = AuthService.getCurrentRole();

        if (role == Role.ROLE_ADMIN) {
            return;
        }

        if (role != Role.ROLE_USER) {
            throw new NoRightsException("The user does not have enough rights");
        }

        User user;
        try {
            user = AuthService.getCurrentUser();
        } catch (CredentialException e) {
            throw new NoRightsException("The user does not have enough rights");
        }

        if (!user.getId().equals(userId)) {
            throw new NoRightsException("The user does not have enough rights");
        }
    }

    /**
     * Checks if the current user has permission to perform actions on a given entity type and entity ID.
     *
     * @param entityType the type of the entity (e.g., "offer", "user", "category")
     * @param entityId   the ID of the entity to check permissions for
     * @throws NoRightsException            if the user does not have enough rights
     * @throws UserIsNotAuthorizedException if the user is not authorized
     * @throws CredentialException          if there is an issue with user credentials
     */
    public void checkUserPermissions(String entityType, Long entityId) {

        switch (EntityTypeWithImages.get(entityType.toLowerCase())) {
            case OFFER -> {
                Long ownerId = offerService.findOwnerIdByOfferId(entityId);
                checkUserPermissions(ownerId);
            }
            case USER -> checkUserPermissions(entityId);
            case CATEGORY -> {
                Role role = AuthService.getCurrentRole();
                if (role != Role.ROLE_ADMIN)
                    throw new NoRightsException("The user does not have enough rights");
            }
        }
    }

    /**
     * Checks if the current user has permission to perform actions on images by their base names.
     *
     * @param baseNames the set of base names of the images
     * @throws NoRightsException            if the user does not have enough rights
     * @throws UserIsNotAuthorizedException if the user is not authorized
     * @throws CredentialException          if there is an issue with user credentials
     */
    public void checkUserPermissionsForImageByBaseName(Set<String> baseNames) {
        baseNames.forEach(baseName -> {
            Set<ImageEntity> images = imageService.findAllImagesByBaseName(baseName);
            checkUserPermissionsForImageByImageEntities(images);
        });
    }

    /**
     * Checks if the current user has permission to perform actions on a set of image entities.
     *
     * @param images the set of ImageEntity objects to check permissions for
     * @throws NoRightsException            if the user does not have enough rights
     * @throws UserIsNotAuthorizedException if the user is not authorized
     * @throws CredentialException          if there is an issue with user credentials
     * @throws PathWrongException           if the image path is incorrect
     */
    public void checkUserPermissionsForImageByImageEntities(Set<ImageEntity> images) {
        String startSub = basePath + ImageConstants.TEMP_IMAGE_DIR + "/";
        Set<Long> ids = new HashSet<>();
        AtomicBoolean isEntityIdIsNull = new AtomicBoolean(true);

        images.forEach(e -> {
            if (e.getEntityId() == null) {
                String tempPath = e.getFullPath();
                if (tempPath.startsWith(startSub)) {
                    String remainingPath = tempPath.substring(startSub.length());
                    int nextSlashIndex = remainingPath.indexOf("/");
                    if (nextSlashIndex != -1) {
                        String id = remainingPath.substring(0, nextSlashIndex);
                        ids.add(Long.parseLong(id));
                    } else throw new PathWrongException();
                } else throw new PathWrongException();
            } else {
                checkUserPermissions(e.getEntityType(), e.getEntityId());
                isEntityIdIsNull.set(false);
            }
        });
        if (isEntityIdIsNull.get()) {
            if (ids.size() != 1) throw new PathWrongException();
            Long userId = ids.iterator().next();
            checkUserPermissions(userId);
        }
    }
}
