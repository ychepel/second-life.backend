package de.ait.secondlife.scheduler;

import de.ait.secondlife.services.interfaces.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class AuctionFinisher {

    private final OfferService offerService;

    @Scheduled(fixedRate = 60000)
    public void finishAuction() {
        offerService.findUnfinishedAuctions().forEach(offerService::finishAuction);
    }
}
