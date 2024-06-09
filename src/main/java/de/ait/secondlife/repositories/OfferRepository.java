package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Page<Offer> findAllByIsActiveTrue(Pageable pageable);

    Optional<Offer> findByIdAndIsActiveTrue(Long offerId);

    Page<Offer> findByUserIdAndIsActiveTrue(Long id, Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE o.auctionFinishedAt <= :currentTime AND o.status.name = :status AND o.isActive = TRUE")
    List<Offer> findFinishedActiveAuctions(LocalDateTime currentTime, OfferStatus status);
}
