package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.Status;
import de.ait.secondlife.services.interfaces.*;
import de.ait.secondlife.services.mapping.OfferMappingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class OfferContextImpl implements OfferContext {

    private OfferService offerService;
    private BidService bidService;
    private final EmailService emailService;

    private Offer offer;

    @Autowired
    public void setOfferContext(@Lazy OfferService offerService) {
        this.offerService = offerService;
    }

    @Autowired
    public void setBidService(@Lazy BidService bidService) {
        this.bidService = bidService;
    }

    @Setter
    private StateStrategy stateStrategy;

    public void setOffer(Offer offer) {
        this.offer = offer;
        setStateFromOfferStatus(offer);
    }

    private void setStateFromOfferStatus(Offer offer) {
        Status status = offer.getStatus();
        if (status == null) {
            this.stateStrategy = new DraftState();
            return;
        }
        OfferStatus offerStatus = status.getName();
        switch (offerStatus) {
            case DRAFT:
                this.stateStrategy = new DraftState();
                break;
            case REJECTED:
                this.stateStrategy = new RejectedState();
                break;
            case VERIFICATION:
                this.stateStrategy = new VerificationState();
                break;
            case AUCTION_STARTED:
                this.stateStrategy = new AuctionStartedState();
                break;
            case AUCTION_FINISHED:
                this.stateStrategy = new AuctionFinishedState();
                break;
            case QUALIFICATION:
                this.stateStrategy = new QualificationState();
                break;
            case COMPLETED:
                this.stateStrategy = new CompleteState();
                break;
            case CANCELED:
                this.stateStrategy = new CancelState();
                break;
            case BLOCKED_BY_ADMIN:
                this.stateStrategy = new BlockByAdminState();
                break;
            default:
                throw new IllegalArgumentException("Unsupported offer status: " + offerStatus);
        }
    }

    public void draft() {
        stateStrategy.draft(this);
    }

    public void reject(Long rejectionReasonId) {
        stateStrategy.reject(this, rejectionReasonId);
    }

    public void verify() {
        stateStrategy.verify(this);
    }

    public void startAuction() {
        stateStrategy.startAuction(this);
    }

    public void finishAuction() {
        stateStrategy.finishAuction(this);
    }

    public void qualify() {
        stateStrategy.qualify(this);
    }

    public void complete(Long winnerBidId) {
        stateStrategy.complete(this, winnerBidId);
    }

    public void cancel() {
        stateStrategy.cancel(this);
    }

    public void blockByAdmin() {
        stateStrategy.blockByAdmin(this);
    }
}
