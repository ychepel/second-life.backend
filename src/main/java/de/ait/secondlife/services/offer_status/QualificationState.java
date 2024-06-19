package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.services.interfaces.BidService;
import de.ait.secondlife.services.interfaces.OfferContext;
import de.ait.secondlife.services.interfaces.OfferService;

import java.util.List;

public class QualificationState extends StateStrategy {

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
        Offer offer = getOfferAllowedForCurrentUser(context);
        if (winnerBidId == null) {
            throw new IllegalStateException(
                    String.format("Trying to complete Offer [ID=%d] with nullable Winner Bid ID", offer.getId())
            );
        }
        if (offer.getWinnerBid() != null) {
            throw new IllegalStateException(
                    String.format(
                            "Offer [ID=%d] already has a winner. Attempt to replace it with new Bid [ID=%d]",
                            offer.getId(),
                            winnerBidId
                    )
            );
        }

        List<Long> existingBids = offer.getBids().stream().map(Bid::getId).toList();
        if (!existingBids.contains(winnerBidId)) {
            throw new IllegalStateException(
                    String.format(
                            "Trying to complete Offer [ID=%d] with non-existent Bid [ID=%d]",
                            offer.getId(),
                            winnerBidId
                    )
            );
        }

        BidService bidService = context.getBidService();
        Bid bid = bidService.getById(winnerBidId);
        offer.setWinnerBid(bid);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.COMPLETED);
        context.setStateStrategy(new CompleteState());
    }

    @Override
    public void qualify(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void cancel(OfferContext context) {
        context.setStateStrategy(new CancelState());
        Offer offer = getOfferAllowedForCurrentUser(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.CANCELED);
        context.setStateStrategy(new CancelState());
    }

    @Override
    public void blockByAdmin(OfferContext context) {
        context.setStateStrategy(new BlockByAdminState());
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.BLOCKED_BY_ADMIN);
        //TODO: mailing - inform offer owner about blocking offer
        context.setStateStrategy(new BlockByAdminState());
    }
}
