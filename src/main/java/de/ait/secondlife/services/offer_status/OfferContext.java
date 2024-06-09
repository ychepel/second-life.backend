package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.domain.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.Status;
import de.ait.secondlife.services.interfaces.AdminService;
import de.ait.secondlife.services.interfaces.BidService;
import de.ait.secondlife.services.interfaces.OfferService;
import de.ait.secondlife.services.interfaces.UserService;
import de.ait.secondlife.services.mapping.OfferMappingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class OfferContext {

    private final OfferService offerService;
    private final UserService userService;
    private final AdminService adminService;
    private final BidService bidService;
    private final OfferMappingService offerMappingService;

    private Offer offer;

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
