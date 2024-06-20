package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.OfferContext;

import javax.security.auth.login.CredentialException;
import java.util.Objects;

public abstract class StateStrategy {

    abstract void draft(OfferContext context);

    abstract void reject(OfferContext context, Long rejectionReasonId);

    abstract void verify(OfferContext context);

    abstract void startAuction(OfferContext context);

    abstract void finishAuction(OfferContext context);

    abstract void complete(OfferContext context, Long winnerBidId);

    abstract void qualify(OfferContext context);

    abstract void cancel(OfferContext context);

    abstract void blockByAdmin(OfferContext context);

    protected Offer getOfferAllowedForCurrentUser(OfferContext context) {
        User currentUser;
        try {
            currentUser = AuthService.getCurrentUser();
        } catch (CredentialException e) {
            throw new IllegalStateException(
                    String.format("Attempt to change Offer [ID=%d] status by not authenticated user", context.getOffer().getId()), e
            );
        }

        Offer offer = context.getOffer();
        User offerUser = offer.getUser();
        if (!offerUser.isActive() || !Objects.equals(offerUser.getId(), currentUser.getId())) {
            throw new IllegalStateException(
                    String.format(
                            "Attempt to change Offer [ID=%d] status by unauthorized User [ID=%d, email=%s]",
                            context.getOffer().getId(),
                            currentUser.getId(),
                            currentUser.getUsername()
                    )
            );
        }
        return offer;
    }

    protected Offer getOfferAllowedForCurrentAdmin(OfferContext context) {
        try {
            AuthService.getCurrentAdmin();
        } catch (CredentialException e) {
            throw new IllegalStateException(
                    String.format("Attempt to change Offer [ID=%d] status by not authenticated admin", context.getOffer().getId()), e
            );
        }
        return context.getOffer();
    }
}
