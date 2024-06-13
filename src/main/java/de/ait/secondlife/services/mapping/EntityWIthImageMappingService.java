package de.ait.secondlife.services.mapping;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.interfaces.EntityWithImage;
import de.ait.secondlife.services.interfaces.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
abstract public class EntityWIthImageMappingService {

    @Autowired
    protected ImageService imageService;

    protected ImagePathsResponseDto getImages(EntityWithImage entityWithImage) {
        String entityName = entityWithImage.getClass().getSimpleName().toLowerCase();
        String entityType = EntityTypeWithImages.get(entityName).getType();
        return imageService.findAllImageForEntity(entityType, entityWithImage.getId());
    }
}
