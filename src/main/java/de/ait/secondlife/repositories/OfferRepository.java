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
import java.util.Set;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    @Query("SELECT o FROM Offer o WHERE" +
            " (:categoryId IS NULL OR o.category.id = :categoryId) " +
            "AND (:offerStatus IS NULL OR o.status.name = :offerStatus ) " +
            "AND (:isFree IS NULL OR o.isFree = :isFree )")
    Page<Offer> findAll(
            @Param("categoryId") Long categoryId,
            @Param("offerStatus") OfferStatus offerStatus,
            @Param("isFree") Boolean isFree,
            Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE" +
            " (:categoryId IS NULL OR o.category.id = :categoryId) " +
            "AND (:offerStatus IS NULL OR o.status.name = :offerStatus ) " +
            "AND (:isFree IS NULL OR o.isFree = :isFree ) " +
            "AND o.user.id =:id")
    Page<Offer> findByUserId(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("offerStatus") OfferStatus offerStatus,
            @Param("isFree") Boolean isFree,
            Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE o.auctionFinishedAt <= :currentTime AND o.status.name = :status")
    List<Offer> findFinishedAuctions(LocalDateTime currentTime, OfferStatus status);

    @Query("SELECT o FROM Offer o" +
            " WHERE (o.status.name = :offerStatus)" +
            " AND (:locationId IS NULL OR o.location.id = :locationId)" +
            " AND (LOWER(o.title) LIKE LOWER(CONCAT('%', :pattern, '%'))" +
            " OR LOWER(o.description) LIKE LOWER(CONCAT('%', :pattern, '%')))")
    Page<Offer> searchAll(OfferStatus offerStatus, Pageable pageable, Long locationId, String pattern);

    @Query("SELECT o FROM Offer o " +
            "JOIN o.bids b " +
            "WHERE " +
            "(:categoryId IS NULL OR o.category.id = :categoryId) " +
            "AND (:offerStatus IS NULL OR o.status.name = :offerStatus) " +
            "AND (:isFree IS NULL OR o.isFree = :isFree) " +
            "AND o.status.name IN :statuses " +
            "AND b.user.id = :id " )
    Page<Offer> findUserAuctionParticipations(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("offerStatus") OfferStatus offerStatus,
            @Param("isFree") Boolean isFree,
            @Param("statuses") Set<OfferStatus> statuses,
            Pageable pageable);
}
