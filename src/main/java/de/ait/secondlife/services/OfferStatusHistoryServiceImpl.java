package de.ait.secondlife.services;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.OfferStatusHistory;
import de.ait.secondlife.domain.entity.RejectionReason;
import de.ait.secondlife.repositories.OfferStatusHistoryRepository;
import de.ait.secondlife.services.interfaces.OfferStatusHistoryService;
import de.ait.secondlife.services.interfaces.RejectionReasonService;
import de.ait.secondlife.services.interfaces.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of the OfferStatusHistoryService interface for managing offer status history.
 *
 * <p>
 * This service class provides methods to create offer status history records.
 * It interacts with repositories for storing status history and fetching status and rejection reasons.
 * </p>
 *
 * <p>
 * This class ensures that an offer ID is present before creating a status history record.(Version 1.0)
 * It supports recording both normal status changes and status changes with associated rejection reasons.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown include:
 * <ul>
 *     <li>{@link IllegalArgumentException} - if attempting to save history for an offer with no ID</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @author Second Life Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class OfferStatusHistoryServiceImpl implements OfferStatusHistoryService {

    private final OfferStatusHistoryRepository historyRepository;
    private final StatusService statusService;
    private final RejectionReasonService rejectionReasonService;

    /**
     * Creates a new offer status history record without a rejection reason.
     *
     * @param offer       the offer entity for which status history is created
     * @param offerStatus the new status of the offer
     * @throws IllegalArgumentException if the offer ID is null
     */
    @Override
    public void create(Offer offer, OfferStatus offerStatus) {
        create(offer, offerStatus, null);
    }

    /**
     * Creates a new offer status history record with an associated rejection reason.
     *
     * @param offer             the offer entity for which status history is created
     * @param offerStatus       the new status of the offer
     * @param rejectionReasonId the ID of the rejection reason, or null if no rejection reason is associated
     * @throws IllegalArgumentException if the offer ID is null
     */
    @Override
    public void create(Offer offer, OfferStatus offerStatus, Long rejectionReasonId) {
        if (offer.getId() == null) {
            throw new IllegalArgumentException(String.format("Trying to save history for an offer with no ID [%s; %s]", offer, offerStatus));
        }

        RejectionReason rejectionReason = rejectionReasonId != null ? rejectionReasonService.getById(rejectionReasonId) : null;
        OfferStatusHistory history = OfferStatusHistory.builder()
                .offer(offer)
                .status(statusService.getByOfferStatus(offerStatus))
                .rejection(rejectionReason)
                .createdAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }
}
