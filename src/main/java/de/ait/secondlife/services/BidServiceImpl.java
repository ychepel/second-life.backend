package de.ait.secondlife.services;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.dto.BidCreationDto;
import de.ait.secondlife.domain.dto.BidResponseDto;
import de.ait.secondlife.domain.dto.BidsResponseDto;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BidCreationException;
import de.ait.secondlife.repositories.BidRepository;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.BidService;
import de.ait.secondlife.services.interfaces.OfferService;
import de.ait.secondlife.services.mapping.BidMappingService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final BidMappingService mappingService;
    private final OfferService offerService;
    private final EntityManager entityManager;

    @Override
    public Bid getById(Long id) {
        return bidRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(BidCreationDto dto) throws CredentialException {
        Offer offer = offerService.findById(dto.getOfferId());

        checkOfferStatus(offer);

        BigDecimal newBidValue = dto.getBidValue();

        if (offer.getIsFree()) {
            checkFreeAuction(newBidValue);
        } else {
            checkNotFreeAuction(offer, newBidValue);
        }

        User user = AuthService.getCurrentUser();
        checkAuthentication(offer, user);

        Bid newBid = mappingService.toEntity(dto);
        newBid.setUser(user);
        newBid.setOffer(offer);
        bidRepository.save(newBid);

        if (isWinningBid(offer, newBidValue)) {
            entityManager.refresh(offer);
            offerService.finishAuction(offer);
        }
    }

    @Override
    public BidsResponseDto findAllByOfferId(Long id) throws CredentialException {
        Offer offer = offerService.findById(id);
        User user = AuthService.getCurrentUser();
        if (!Objects.equals(offer.getUser().getId(), user.getId())) {
            throw new UserIsNotAuthorizedException("The non-owner is not authorized to view list of bids");
        }
        List<BidResponseDto> bids = offer.getBids()
                .stream()
                .sorted(Comparator.comparing(Bid::getId).reversed())
                .map(mappingService::toDto)
                .toList();
        BidsResponseDto response = new BidsResponseDto();
        response.setBids(bids);
        return response;
    }

    private boolean isWinningBid(Offer offer, BigDecimal newBidValue) {
        if (offer.getIsFree()) {
            return false;
        }

        BigDecimal offerWinBid = offer.getWinBid();
        if (offerWinBid == null) {
            return false;
        }
        return newBidValue.compareTo(offerWinBid) == 0;
    }

    private void checkAuthentication(Offer offer, User user) {
        if (Objects.equals(offer.getUser().getId(), user.getId())) {
            throw new BidCreationException("Bid cannot be created by offer owner");
        }
        if (offer.getIsFree()) {
            List<Long> auctionParticipantIds = offer.getBids()
                    .stream()
                    .map(bid -> bid.getUser().getId())
                    .toList();
            if (auctionParticipantIds.contains(user.getId())) {
                throw new BidCreationException("Bid cannot be created twice by one user in free auction");
            }
        }
    }

    private void checkOfferStatus(Offer offer) {
        if (offer.getOfferStatus() != OfferStatus.AUCTION_STARTED) {
            throw new BidCreationException("Bid cannot be created for offer not in status AUCTION_STARTED");
        }
    }

    private void checkFreeAuction(BigDecimal newBidValue) {
        if (newBidValue.compareTo(BigDecimal.ZERO) > 0) {
            throw new BidCreationException("Bid with value greater than 0 cannot be created for the free offer");
        }
    }

    private void checkNotFreeAuction(Offer offer, BigDecimal newBidValue) {
        BigDecimal existingMaxBidValue = offer.getMaxBidValue();
        if (existingMaxBidValue == null) {
            if (newBidValue.compareTo(offer.getStartPrice()) < 0) {
                throw new BidCreationException("Bid cannot be less than auction start price");
            }
        } else if (newBidValue.compareTo(existingMaxBidValue) <= 0) {
            throw new BidCreationException("Bid cannot be less or equal to current maximum bid value");
        }

        BigDecimal winBid = offer.getWinBid();
        if (winBid != null && newBidValue.compareTo(winBid) > 0) {
            throw new BidCreationException("Bid cannot be greater than buyout price (win bid value)");
        }
    }
}
