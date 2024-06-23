package de.ait.secondlife.services.utilities;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.domain.entity.ImageEntity;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.NoRightsException;
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

@Component
public class UserPermissionsUtilities  {

    @Lazy
    @Autowired
    private OfferService offerService;
    @Lazy
    @Autowired
    private ImageService imageService;

    @Value("${do.base.path}")
    private String basePath;

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

    public void checkUserPermissionsForImageByBaseName(Set<String> baseNames) {
        baseNames.forEach(baseName -> {

            Set<ImageEntity> images = imageService.findAllImagesByBaseName(baseName);
            String startSub = basePath + ImageConstants.TEMP_IMAGE_DIR + "/";
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
            Long userId = ids.iterator().next();
            checkUserPermissions(userId);
        });
    }
}
