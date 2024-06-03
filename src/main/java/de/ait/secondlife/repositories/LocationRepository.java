package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
