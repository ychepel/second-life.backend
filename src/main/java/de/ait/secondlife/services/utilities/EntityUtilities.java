package de.ait.secondlife.services.utilities;

import de.ait.secondlife.constants.EntityTypeWithImgs;
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

    public boolean checkEntityById(String entityType, Long entityId) {
        if(entityId==null) return true;
        CheckEntityExistsService service;
        switch (EntityTypeWithImgs.get(entityType.toLowerCase())) {
            case OFFER -> service = offerService;
            case USER -> service = userService;
            case CATEGORY -> service = categoryService;
            default -> throw new BadEntityTypeException(entityType);
        }
        return service.checkEntityExistsById(entityId);
    }
}
