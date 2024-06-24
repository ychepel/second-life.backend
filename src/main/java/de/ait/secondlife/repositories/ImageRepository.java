package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ImageRepository extends JpaRepository<ImageEntity,Long> {

    Set<ImageEntity> findAllByEntityIdAndEntityType(Long id, String entityType);

    Set<ImageEntity> findAllByBaseName(String baseName);

    List<ImageEntity> findAllByEntityIdIsNullAndCreatedAtLessThan(LocalDateTime dateTime);

    void deleteAllByBaseName(String baseName);

    void deleteAllByBaseNameIn(Set<String> baseNames);
}
