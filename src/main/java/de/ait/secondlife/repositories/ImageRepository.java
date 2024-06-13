package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ImageRepository extends JpaRepository<ImageEntity,Long> {

    Set<ImageEntity> findAllByEntityIdAndEntityType(Long id, String entityType);

    Set<ImageEntity> findAllByBaseName(String baseName);

    void deleteAllByBaseName(String baseName);
}
