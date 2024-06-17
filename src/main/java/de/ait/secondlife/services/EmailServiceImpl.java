package de.ait.secondlife.services;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.domain.entity.Notification;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.repositories.NotificationRepository;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.emails.EmailTemplateServiceFactory;
import de.ait.secondlife.services.interfaces.EmailService;
import de.ait.secondlife.services.interfaces.EmailTemplateService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private JavaMailSender sender;
    private Configuration mailConfig;
    private NotificationRepository notificationRepository;
    private AuthService authService;
    private EmailTemplateServiceFactory templateServiceFactory;

    private static final String TEMPLATES_PATH = "/mail/";

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailServiceImpl(
            JavaMailSender sender,
            Configuration mailConfig,
            NotificationRepository notificationRepository,
            AuthService authService,
            EmailTemplateServiceFactory templateServiceFactory
    ) {
        this.sender = sender;
        this.mailConfig = mailConfig;
        this.notificationRepository = notificationRepository;
        this.authService = authService;
        this.templateServiceFactory = templateServiceFactory;

        mailConfig.setDefaultEncoding("UTF-8");
        mailConfig.setTemplateLoader(new ClassTemplateLoader(EmailServiceImpl.class, TEMPLATES_PATH));
    }

    @Override
    public void createNotification(AuthenticatedUser authenticatedUser, NotificationType notificationType) {
        Notification newNotification = Notification.builder()
                .createdAt(LocalDateTime.now())
                .notificationType(notificationType)
                .authenticatedUserId(authenticatedUser.getId())
                .receiverRole(authenticatedUser.getRole())
                .build();

        notificationRepository.save(newNotification);
    }

    @Override
    public void createNotification(AuthenticatedUser authenticatedUser, NotificationType notificationType, Long contextId) {
        Notification newNotification = Notification.builder()
                .createdAt(LocalDateTime.now())
                .notificationType(notificationType)
                .authenticatedUserId(authenticatedUser.getId())
                .receiverRole(authenticatedUser.getRole())
                .contextId(contextId)
                .build();

        notificationRepository.save(newNotification);
    }

    @Override
    public void sendPendingEmails() {
        Notification pendingEmail = notificationRepository.findFirstBySentAtIsNull();
        while (pendingEmail != null) {
            pendingEmail.setSentAt(LocalDateTime.now());
            notificationRepository.save(pendingEmail);

            try {
                sendEmail(pendingEmail);
            } catch (Exception e) {
                log.error("Failed to send email", e);
                pendingEmail.setSentAt(null);
                notificationRepository.save(pendingEmail);
                return;
            }

            pendingEmail = notificationRepository.findFirstBySentAtIsNull();
        }
    }

    private void sendEmail(Notification notification) throws Exception {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        EmailTemplateService templateService = templateServiceFactory.createService(
                notification.getNotificationType(),
                mailConfig,
                notification
        );

        helper.setFrom(senderEmail);

        AuthenticatedUser authenticatedUser = authService.getAuthenticatedUser(
                notification.getReceiverRole(),
                notification.getAuthenticatedUserId()
        );
        helper.setTo(authenticatedUser.getEmail());

        helper.setSubject(templateService.getSubject());
        helper.setText(templateService.getBody(), true);

        sender.send(message);
    }
}
