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
//TODO: PR review - rename class to UserPermissionsUtilities
public class UserCredentialsUtilities implements ImageConstants {
    @Lazy
    @Autowired
    private  OfferService offerService;
    @Lazy
    @Autowired
    private  ImageService imageService;

    @Value("${do.base.path}")
    private String basePath;

    //TODO: PR review - rename method to checkUserPermissions
    public void checkUserCredentials(Long id ) { //TODO: PR review rename argument to userId
        try {
            Role role = AuthService.getCurrentRole();

            if (role == Role.ROLE_ADMIN) {
                return;
            }
            if (role == Role.ROLE_USER) {
                User user = AuthService.getCurrentUser();
                if (!user.getId().equals(id)) {
                    throw new NoRightsException("The user does not have enough rights");
                }
            }
        } catch (CredentialException ignored) {
        }
    }

    public void checkUserCredentials(String entityType, Long entityId) {
        if (entityId == null) return;
        switch (EntityTypeWithImages.get(entityType.toLowerCase())) {
            case OFFER -> {
                Long ownerId = offerService.findOwnerIdByOfferId(entityId);
                checkUserCredentials(ownerId);
            }
            case USER -> checkUserCredentials(entityId);
            case CATEGORY -> {
                //TODO: PR review - we need to check only role (equals or not to ADMIN)
                try {
                    User user = AuthService.getCurrentUser();
                    checkUserCredentials(user.getId());
                } catch (CredentialException ignored) {
                }
            }
        }
    }

    public void checkUserCredentials(Set<String> baseNames) {
        baseNames.forEach(baseName -> {

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
            Long userId = ids.iterator().next();
            checkUserCredentials(userId);
        });
    }
}
