package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.services.offer_status.StateStrategy;

public interface OfferContext {

    OfferService getOfferService();

    UserService getUserService();

    AdminService getAdminService();

    BidService getBidService();

    void setStateStrategy(StateStrategy stateStrategy);

    void setOffer(Offer offer);

    Offer getOffer();

    void draft();

    void reject(Long rejectionReasonId);

    void verify();

    void startAuction();

    void finishAuction();

    void qualify();

    void complete(Long winnerBidId);

    void cancel();

    void blockByAdmin();
}
