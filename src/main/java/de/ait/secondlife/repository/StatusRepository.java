package de.ait.secondlife.repository;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StatusRepository extends JpaRepository<Status, Long> {

    Optional<Status> findByName(String name);
}
