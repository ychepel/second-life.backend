package de.ait.secondlife.mailing;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.repositories.NotificationRepository;
import de.ait.secondlife.services.interfaces.ConfirmationService;
import de.ait.secondlife.services.interfaces.EmailService;
import de.ait.secondlife.services.interfaces.OfferService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private JavaMailSender sender;
    private Configuration mailConfig;
    private ConfirmationService confirmationService;
    private OfferService offerService;
    private NotificationRepository notificationRepository;
    private EmailTemplateFactory emailTemplateFactory;

    private static final String TEMPLATES_PATH = "/mail/";

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailServiceImpl(

            JavaMailSender sender,
            Configuration mailConfig,
            OfferService offerService,
            ConfirmationService confirmationService,
            NotificationRepository notificationRepository)
            throws IOException {

        this.sender = sender;
        this.mailConfig = mailConfig;
        this.offerService = offerService;
        this.confirmationService = confirmationService;
        this.notificationRepository = notificationRepository;
        this.emailTemplateFactory = new EmailTemplateFactory(mailConfig);

        mailConfig.setDefaultEncoding("UTF-8");
        mailConfig.setTemplateLoader(new ClassTemplateLoader(EmailServiceImpl.class, TEMPLATES_PATH));
    }

    @Override
    public void sendConfirmationEmailToFinishRegistration(User user) {
        sendEmail(user, NotificationType.REGISTRATION_EMAIL, null);
    }


    public void sendEmail(User user, NotificationType notificationType, Offer offer) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        String text = generateEmailContent(user, notificationType, offer);

        try {
            helper.setFrom(senderEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(getSubject(notificationType));
            helper.setText(text, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }

        sender.send(message);
    }

    @Override
    public void createNotification(User user, NotificationType notificationType) {
        Notification newNotification = Notification.builder()
                .createdAt(LocalDateTime.now())
                .notificationType(notificationType)
                .user(user)
                .build();

        notificationRepository.save(newNotification);
    }

    @Override
    public void sendPendingEmail() {
        List<Notification> pendingEmails = notificationRepository.findAllBySentAtIsNull();

        for(Notification pendingEmail:pendingEmails){
            pendingEmail.setSentAt(LocalDateTime.now());
            sendEmail(pendingEmail.getUser(), pendingEmail.getNotificationType());
            notificationRepository.save(pendingEmail);
        }
    }

    private String generateEmailContent(User user, NotificationType notificationType, Offer offer) {
        try {
            Template template = emailTemplateFactory.getTemplate(notificationType);

            Map<String, Object> model = new HashMap<>();
            model.put("name", user.getFirstName() + " " + user.getLastName());

            if (notificationType == NotificationType.REGISTRATION_EMAIL) {
                String code = confirmationService.generateConfirmationCode(user);
                String url = "https://second-life-app-y2el9.ondigitalocean.app/api/v1/users/" + user.getId() + "/set-active?code=" + code;
                model.put("link", url);
            }

            if (notificationType == NotificationType.OFFER_VERIFICATION_EMAIL) {
                String url = "https://second-life.space/#/admin/offers/"+offer.getId();
                model.put("link", url);
            }

            if (notificationType == NotificationType.REJECTED_OFFER_EMAIL) {
                String url = "https://second-life.space/#/offers/"+offer.getId();
                model.put("link", url);
            }

            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate email content", e);
        }
    }

    private String getSubject(NotificationType notificationType) {
        return switch (notificationType) {
            case REGISTRATION_EMAIL -> "Registration: We need you to confirm your e-mail address";
            case OFFER_VERIFICATION_EMAIL -> "New offer for verification";
            case REJECTED_OFFER_EMAIL -> "Your offer was rejected by Admin and returned to draft status";
            default -> throw new IllegalArgumentException("Unknown notification type: " + notificationType);
        };
    }
}
