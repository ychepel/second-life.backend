package de.ait.secondlife.services.utilities;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;
import de.ait.secondlife.services.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.security.auth.login.CredentialException;


@Component
@RequiredArgsConstructor
public class EntityUtilities {

    private final OfferService offerService;
    private final UserService userService;
    private final CategoryService categoryService;

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
        User user = null;
        try {
            user = userService.getAuthenticatedUser();
        } catch (CredentialException ignored) {
        }
        assert user != null;
        return user.getId();
    }
}
