package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.services.interfaces.ConfirmationService;
import de.ait.secondlife.services.interfaces.EmailService;
import freemarker.template.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender sender;
    private final Configuration mailConfig;
    private final ConfirmationService confirmationService;
    @Override
    public void sendConfirmationEmailToFinishRegistration(User user) {

    }
}
