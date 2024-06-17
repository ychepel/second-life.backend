package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.services.interfaces.OfferService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuctionStartedState extends StateStrategy {

    @Override
    public void draft(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    void reject(OfferContext context, Long rejectionReasonId) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void verify(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void startAuction(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void finishAuction(OfferContext context) {
        Offer offer = context.getOffer();
        log.info(String.format("Finishing auction for Offer [ID=%d]", offer.getId()));
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.AUCTION_FINISHED);
        context.setStateStrategy(new AuctionFinishedState());
        context.complete(null);
    }

    @Override
    public void complete(OfferContext context, Long winnerBidId) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void qualify(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void cancel(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentUser(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.CANCELED);
        //TODO: mailing - inform all participants that auction was canceled
        context.setStateStrategy(new CancelState());
    }

    @Override
    public void blockByAdmin(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.BLOCKED_BY_ADMIN);
        //TODO: mailing - inform offer owner about blocking offer
        context.setStateStrategy(new BlockByAdminState());
    }
}
