package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.EntityImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ImageRepository extends JpaRepository<EntityImage,Long> {

    Set<EntityImage> findAllByEntityIdAndEntityType(Long id, String entityType);
}
