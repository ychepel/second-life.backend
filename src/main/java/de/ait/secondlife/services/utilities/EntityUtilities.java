package de.ait.secondlife.services.utilities;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;
import de.ait.secondlife.services.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class EntityUtilities {

    private final OfferService offerService;
    private final UserService userService;
    private final CategoryService categoryService;

    public void isEntityExists(String entityType, Long entityId) {
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
