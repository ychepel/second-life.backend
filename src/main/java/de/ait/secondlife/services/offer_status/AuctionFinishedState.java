package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.services.interfaces.OfferService;

import java.util.List;

public class AuctionFinishedState extends StateStrategy {

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
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void complete(OfferContext context, Long winnerBidId) {
        Offer offer = context.getOffer();
        OfferService offerService = context.getOfferService();
        List<Bid> bids = offer.getBids();
        if (bids == null || bids.isEmpty()) {
            offerService.setStatus(offer, OfferStatus.COMPLETED);
            //TODO: mailing - inform offer owner about finishing auction without winners
            context.setStateStrategy(new CompleteState());
        } else if (bids.size() == 1) {
            if (offer.getWinnerBid() != null) {
                throw new IllegalStateException(String.format("Offer [ID=%d] already has a winner", offer.getId()));
            }
            offer.setWinnerBid(bids.get(0));
            offerService.setStatus(offer, OfferStatus.COMPLETED);
            //TODO: mailing - inform offer owner about finishing auction with winner
            //TODO: mailing - inform bid owner, that he/she is the winner of the auction
            context.setStateStrategy(new CompleteState());
        } else {
            qualify(context);
        }
    }

    @Override
    public void qualify(OfferContext context) {
        Offer offer = context.getOffer();
        List<Bid> bids = offer.getBids();
        if (bids.size() <= 1) {
            throw new IllegalStateException(
                    String.format("Offer [ID=<%d>] has no/one winner and not require status [%s]", offer.getId(), OfferStatus.QUALIFICATION)
            );
        }
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.QUALIFICATION);
        //TODO: mailing - inform offer owner about need to choose a winner
        context.setStateStrategy(new QualificationState());
    }

    @Override
    public void cancel(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentUser(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.CANCELED);
        offer.setIsActive(false);
        //TODO: mailing - inform all participants that auction was canceled
        context.setStateStrategy(new CancelState());
    }

    @Override
    public void blockByAdmin(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }
}
