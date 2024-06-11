package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.ImageCreateDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;


public interface ImageService {

    void saveNewImage(ImageCreateDto dto) ;

    ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId);

    void deleteImage(String fileNme);
}
