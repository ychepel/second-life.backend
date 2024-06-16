package de.ait.secondlife.services.utilities;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.entity.ImageEntity;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.NoRightToChangeException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.PathWrongException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.services.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.security.auth.login.CredentialException;
import java.util.HashSet;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class EntityUtilities implements ImageConstants {

    private final OfferService offerService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ImageService imageService;

    @Value("${do.base.path}")
    private String basePath;

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

    public Long getCurrentUserId() {
        try {
            return userService.getAuthenticatedUser().getId();
        } catch (CredentialException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkUserCredentials(Long id) {
        try {
            User user = userService.getAuthenticatedUser();
            if (!user.getId().equals(id))
                if (!user.getAuthorities().contains(Role.ROLE_ADMIN))
                    throw new NoRightToChangeException("User is not authorised for this operation");
        } catch (CredentialException ignored) {
        }
    }

    public void checkCredentials(String entityType, Long entityId) {
        if (entityId == null) return;
        switch (EntityTypeWithImages.get(entityType.toLowerCase())) {
            case OFFER -> {
                Long ownerId = offerService.findOwnerIdByOfferId(entityId);
                checkUserCredentials(ownerId);
            }
            case USER -> checkUserCredentials(entityId);
            case CATEGORY -> {
                try {
                    User user = userService.getAuthenticatedUser();
                    if (!user.getAuthorities().contains(Role.ROLE_ADMIN))
                        throw new NoRightToChangeException("User is not authorised for this operation");
                } catch (CredentialException ignored) {
                }
            }
        }
    }

    public void checkCredentials(String baseName) {
        Set<ImageEntity> images = imageService.findAllImagesByBaseName(baseName);
        images.forEach(e -> checkCredentials(e.getEntityType(), e.getEntityId()));
    }

    private Long getUserIdBaseName(String baseName) {
        Set<ImageEntity> images = imageService.findAllImagesByBaseName(baseName);
        String startSub = basePath + TEMP_IMAGE_DIR + "/";
        Set<Long> ids = new HashSet<>();
        images.forEach(e -> {
            String tempPath = e.getFullPath();
            if (tempPath.startsWith(startSub)) {
                String remainingPath = tempPath.substring(startSub.length());
                int nextSlashIndex = remainingPath.indexOf("/");
                if (nextSlashIndex != -1) {
                    String id = remainingPath.substring(0, nextSlashIndex);
                    ids.add(Long.parseLong(id));
                } else throw new PathWrongException();

            } else throw new PathWrongException();
        });
        if (ids.size() != 1) throw new PathWrongException();
        return ids.iterator().next();
    }

    public void checkCredentialsToConnectImageToEntity(Set<String> baseNames) {
        baseNames.forEach(baseName -> {
                    Long userId = getUserIdBaseName(baseName);
                    checkUserCredentials(userId);
                }
        );
    }
}
