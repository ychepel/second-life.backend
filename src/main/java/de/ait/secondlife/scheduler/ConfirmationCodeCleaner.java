package de.ait.secondlife.scheduler;

import de.ait.secondlife.services.interfaces.ConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ConfirmationCodeCleaner {

    private final ConfirmationService service;

    @Scheduled(cron = "0 5 4 * * *")
    public void clean(){
        service.deleteAllExpired();
    }
}
