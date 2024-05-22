package de.ait.secondlife.repository;

import de.ait.secondlife.domain.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

    @Query("SELECT e FROM Offer e WHERE e.isActive = true")
    Page<Offer> findAllWithPagination(Pageable pageable);
}
