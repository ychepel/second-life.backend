package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;

public interface OfferStatusHistoryService {

    void create(Offer offer, OfferStatus offerStatus);

    void create(Offer offer, OfferStatus offerStatus, Long rejectionReasonId);
}
