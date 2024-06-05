package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.ImagePropsResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

public interface ImageService {

    void saveNewImage(MultipartFile file, String entityType,Long entityId) ;

    Set<ImagePropsResponseDto> findAllImageForEntity(String entityType, Long entityId);

    InputStream getImage(String size, String baseName) ;
}
