package de.ait.secondlife.services;

import de.ait.secondlife.domain.constants.OfferStatus;
import de.ait.secondlife.domain.dto.BidCreationDto;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BidCreationException;
import de.ait.secondlife.repositories.BidRepository;
import de.ait.secondlife.services.interfaces.BidService;
import de.ait.secondlife.services.interfaces.OfferService;
import de.ait.secondlife.services.interfaces.UserService;
import de.ait.secondlife.services.mapping.BidMappingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final UserService userService;
    private final BidMappingService mappingService;
    private final OfferService offerService;

    @Override
    public Bid getById(Long id) {
        return bidRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(BidCreationDto dto) throws CredentialException {
        Offer offer = offerService.findById(dto.getOfferId());
        if (offer.getIsFree()) {
            throw new BidCreationException("Bid cannot be created for free offer");
        }
        if (offer.getOfferStatus() != OfferStatus.AUCTION_STARTED) {
            throw new BidCreationException("Bid cannot be created for offer not in status AUCTION_STARTED");
        }

        BigDecimal newBidValue = dto.getBidValue();
        BigDecimal existingMaxBidValue = offer.getMaxBidValue();
        if (existingMaxBidValue == null) {
            if (newBidValue.compareTo(offer.getStartPrice()) < 0) {
                throw new BidCreationException("Bid cannot be less than auction start price");
            }
        } else if (newBidValue.compareTo(existingMaxBidValue) <= 0) {
            throw new BidCreationException("Bid cannot be less or equal to current maximum bid value");
        }

        if (newBidValue.compareTo(offer.getWinBid()) > 0) {
            throw new BidCreationException("Bid cannot be greater than buyout price (win bid value)");
        }

        User user = userService.getAuthenticatedUser();
        if (Objects.equals(offer.getUser().getId(), user.getId())) {
            throw new BidCreationException("Bid cannot be created by offer owner");
        }

        Bid newBid = mappingService.toEntity(dto);
        newBid.setUser(user);
        newBid.setOffer(offer);
        bidRepository.save(newBid);

        if (newBidValue.compareTo(offer.getWinBid()) == 0) {
            offerService.finishAuction(offer);
        }
    }
}
