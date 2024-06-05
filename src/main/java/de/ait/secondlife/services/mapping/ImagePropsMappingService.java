package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.ImagePropsResponseDto;
import de.ait.secondlife.domain.entity.EntityImage;
import org.mapstruct.Mapper;

import java.awt.*;

@Mapper
public interface ImagePropsMappingService {

    ImagePropsResponseDto toDto(EntityImage image);
}
