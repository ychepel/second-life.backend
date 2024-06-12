package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.ImageCreationDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;


public interface ImageService {

    void saveNewImage(ImageCreationDto dto) ;

    ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId);

    void deleteImage(String fileNme);
}
