package de.ait.secondlife.controllers;

import de.ait.secondlife.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//TODO: remove after cleaning db from incorrect emails
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/send")
    public String send() {
        emailService.sendPendingEmails();
        return "Sending emails finished.";
    }
}
