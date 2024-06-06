package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.ImageCreateDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;


import java.io.InputStream;


public interface ImageService {

    void saveNewImage(ImageCreateDto dto) ;

    ImagePathsResponseDto findAllImageForEntity(String entityType, Long entityId);

    InputStream getImage(String fileName);

    void deleteImage(String fileNme);

}
