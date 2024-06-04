package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.EntityImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<EntityImage,Long> {
}
