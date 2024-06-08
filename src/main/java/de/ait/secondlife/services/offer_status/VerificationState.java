package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.domain.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.services.interfaces.AuthenticatedUserService;
import de.ait.secondlife.services.interfaces.OfferService;

import java.time.LocalDateTime;

public class VerificationState extends StateStrategy {

    @Override
    public void draft(OfferContext context, Long rejectionReasonId) {
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        OfferService offerService = context.getOfferService();
        Role role = AuthenticatedUserService.getAuthenticatedUserRole();
        if (Role.ROLE_ADMIN.equals(role)) {
            offerService.setStatus(offer, OfferStatus.DRAFT, rejectionReasonId);
            //TODO: mailing - inform offer owner about rejection
        } else if (Role.ROLE_USER.equals(role)) {
            offerService.setStatus(offer, OfferStatus.DRAFT);
        } else {
            throw new IllegalStateException(
                    String.format("Attempt to change Offer [ID=%d] status with undefined Role [%s]", offer.getId(), role)
            );
        }
        context.setStateStrategy(new DraftState());
    }

    @Override
    public void verify(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void startAuction(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        LocalDateTime auctionFinishedAt = LocalDateTime.now().plusDays(offer.getAuctionDurationDays());
        offer.setAuctionFinishedAt(auctionFinishedAt);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.AUCTION_STARTED);
        //TODO: mailing - inform offer owner about auction started
        context.setStateStrategy(new AuctionStartedState());
    }

    @Override
    public void finishAuction(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
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
        offer.setIsActive(false);
    }

    @Override
    public void blockByAdmin(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.BLOCKED_BY_ADMIN);
        offer.setIsActive(false);
        //TODO: mailing - inform offer owner about blocking offer
        context.setStateStrategy(new BlockByAdminState());
    }
}
