package de.ait.secondlife.repository;

import de.ait.secondlife.domain.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {


    Page<Offer> findAllByIsActiveTrue(Pageable pageable);

    Optional<Offer> findByIdAndIsActiveTrue(UUID offerId);

    Page<Offer> findByUserIdAndIsActiveTrue(Long id, Pageable pageable);
}
