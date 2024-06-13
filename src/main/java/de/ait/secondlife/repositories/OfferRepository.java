package de.ait.secondlife.repositories;


import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    @Query("SELECT o FROM Offer o WHERE" +
            " (:categoryId IS NULL OR o.category.id = :categoryId) " +
            "AND (:offerStatus IS NULL OR o.status.name = :offerStatus ) " +
            "AND (:isFree IS NULL OR o.isFree = :isFree ) " +
            " AND o.isActive = true")
    Page<Offer> findAllActiveWithFiltration(
            @Param("categoryId") Long categoryId,
            @Param("offerStatus") OfferStatus offerStatus,
            @Param("isFree") Boolean isFree,
            Pageable pageable);

    Optional<Offer> findByIdAndIsActiveTrue(Long offerId);

    @Query("SELECT o FROM Offer o WHERE" +
            " (:categoryId IS NULL OR o.category.id = :categoryId) " +
            "AND (:offerStatus IS NULL OR o.status.name = :offerStatus ) " +
            "AND (:isFree IS NULL OR o.isFree = :isFree ) " +
            "AND o.user.id =:id"+
            " AND o.isActive = true")
    Page<Offer> findByUserIdAndIsActiveTrue(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("offerStatus") OfferStatus offerStatus,
            @Param("isFree") Boolean isFree,
            Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE o.auctionFinishedAt <= :currentTime AND o.status.name = :status AND o.isActive = TRUE")
    List<Offer> findFinishedActiveAuctions(LocalDateTime currentTime, OfferStatus status);

    boolean existsByIdAndIsActiveTrue(Long id);
}
