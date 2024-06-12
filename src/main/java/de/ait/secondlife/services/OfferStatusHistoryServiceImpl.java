package de.ait.secondlife.services;

import de.ait.secondlife.domain.constants.OfferStatus;
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

@Service
@RequiredArgsConstructor
public class OfferStatusHistoryServiceImpl implements OfferStatusHistoryService {

    private final OfferStatusHistoryRepository historyRepository;
    private final StatusService statusService;
    private final RejectionReasonService rejectionReasonService;

    @Override
    public void create(Offer offer, OfferStatus offerStatus) {
        create(offer, offerStatus, null);
    }

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
