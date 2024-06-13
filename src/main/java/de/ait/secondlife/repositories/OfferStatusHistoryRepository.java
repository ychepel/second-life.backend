package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.OfferStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferStatusHistoryRepository extends JpaRepository<OfferStatusHistory, Long> {

    List<OfferStatusHistory> findByOfferId(Long offerId);
}
