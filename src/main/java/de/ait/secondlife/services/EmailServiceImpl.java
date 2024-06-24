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

/**
 * Service implementation for sending emails.(Version 1.0)
 * This service manages the creation and sending of notification emails to users.
 *
 * <p>
 * This service interacts with the JavaMailSender, NotificationRepository,
 * AuthService, and EmailTemplateServiceFactory.
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @version 1.0
 * @author: Second Life Team
 */
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

    @Value("${spring.application.name}")
    private String applicationName;

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

    /**
     * Creates a notification for the authenticated user.
     *
     * @param authenticatedUser the authenticated user for whom the notification is created.
     * @param notificationType  the type of notification to create.
     */
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

    /**
     * Creates a notification for the authenticated user with a context ID.
     *
     * @param authenticatedUser the authenticated user for whom the notification is created.
     * @param notificationType  the type of notification to create.
     * @param contextId         the context ID associated with the notification.
     */
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

    /**
     * Sends pending emails by retrieving unsent notifications from the repository.
     * It attempts to send each notification email and updates the sent status accordingly.
     */
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

    /**
     * Sends an email using the provided notification details.
     *
     * @param notification the notification containing email details.
     * @throws Exception if there is an error while sending the email.
     */
    private void sendEmail(Notification notification) throws Exception {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        EmailTemplateService templateService = templateServiceFactory.createService(
                notification.getNotificationType(),
                mailConfig,
                notification
        );

        helper.setFrom(getSender());

        AuthenticatedUser authenticatedUser = authService.getAuthenticatedUser(
                notification.getReceiverRole(),
                notification.getAuthenticatedUserId()
        );
        helper.setTo(authenticatedUser.getEmail());

        helper.setSubject(templateService.getSubject());
        String body = templateService.getBody();
        helper.setText(body, true);

        sender.send(message);
    }

    /**
     * Retrieves the sender email address formatted with the application name.
     *
     * @return the formatted sender email address.
     */
    private String getSender() {
        return String.format("%s <%s>", applicationName, senderEmail);
    }
}
