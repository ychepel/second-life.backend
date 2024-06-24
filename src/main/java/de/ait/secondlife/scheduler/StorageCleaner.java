package de.ait.secondlife.scheduler;

import de.ait.secondlife.services.interfaces.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class StorageCleaner {

    private final ImageService service;

    @Scheduled(cron = "0 10 4 * * *")
    public void clean(){
        service.deleteUnattachedImages();
    }
}
