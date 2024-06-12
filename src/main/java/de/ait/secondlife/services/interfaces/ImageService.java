package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.ImageCreationDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;

import java.util.Set;


public interface ImageService {

    ImagePathsResponseDto saveNewImage(String entityType, Long entityId, ImageCreationDto dto) ;

    ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId);

    void connectTempImgsToEntity(Set<String> baseNames,  Long entityId);

    void deleteImage(String baseName);
}
