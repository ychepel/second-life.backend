package de.ait.secondlife.services.interfaces;


import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Status;

public interface StatusService {

    Status getStatusById(Long id);
    Status getByOfferStatus(OfferStatus offerStatus);
}
