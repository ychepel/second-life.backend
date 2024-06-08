package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Page<Offer> findAllByIsActiveTrue(Pageable pageable);

    Optional<Offer> findByIdAndIsActiveTrue(Long offerId);

    Page<Offer> findByUserIdAndIsActiveTrue(Long id, Pageable pageable);

    List<Offer> findByAuctionFinishedAtLessThanEqualAndStatusNameAndIsActiveTrue(
            LocalDateTime currentTime,
            OfferStatus status
    );
}
