package de.ait.secondlife.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

public interface ImageService {

    void saveNewImage(MultipartFile file, String entityType,Long entityId) ;

    Set<String> findAllImageForEntity(String entityType, Long entityId);

    InputStream getImage(String size, String baseName) ;
}
