package de.ait.secondlife.scheduler;

import de.ait.secondlife.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class EmailSender {

    private final EmailService emailService;

//    @Scheduled(fixedRate = 60000)
    public void sendEmail(){
        emailService.sendPendingEmails();
    }
}
