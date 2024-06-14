package de.ait.secondlife.services;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.domain.entity.Notification;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.repositories.NotificationRepository;
import de.ait.secondlife.services.interfaces.ConfirmationService;
import de.ait.secondlife.services.interfaces.EmailService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
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

    private NotificationRepository notificationRepository;

    private static final String TEMPLATES_PATH = "/mail/";

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailServiceImpl(JavaMailSender sender, Configuration mailConfig, ConfirmationService confirmationService,NotificationRepository notificationRepository) throws IOException {
        this.sender = sender;
        this.mailConfig = mailConfig;
        this.confirmationService = confirmationService;
        this.notificationRepository = notificationRepository;

        mailConfig.setDefaultEncoding("UTF-8");
        mailConfig.setTemplateLoader(new ClassTemplateLoader(EmailServiceImpl.class, TEMPLATES_PATH));

    }

    @Override
    public void sendConfirmationEmailToFinishRegistration(User user) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        String text = generateConfirmationEmail(user);

        try {
            helper.setFrom(senderEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Registration: We need you to confirm your e-mail address");
            helper.setText(text,true);
        }catch (Exception e){
            throw new RuntimeException("Failed to send confirmation email", e);
        }

        sender.send(message);
    }

    @Override
    public void createNotification(User user, NotificationType notificationType) {
        Notification newNotification =  Notification.builder()
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
            //
        }
    }


    private String generateConfirmationEmail(User user){
        try {
            Template template = mailConfig.getTemplate("confirmation_registration_mail.ftlh");

            String code = confirmationService.generateConfirmationCode(user);
            String url = "https://second-life-app-y2el9.ondigitalocean.app/api/v1/users/"+user.getId()+"/set-active"+"?code=" +code;

            Map<String, Object> model = new HashMap<>();
            model.put("name",user.getFirstName()+" "+user.getLastName());
            model.put("link",url);

            return FreeMarkerTemplateUtils.processTemplateIntoString(template,model);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    //Написать метод универсальный для формирования email.
    // Разбить типы имейлов на классы.
    // Сделать абстрактный класс и найти способ вызвать нужный класс. Написать фабрику
    // MimeMessage message = sender.createMimeMessage();
    //        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
    //        String text = generateConfirmationEmail(user);
    //
    //        try {
    //            helper.setFrom(senderEmail);
    //            helper.setTo(user.getEmail());
    //            helper.setSubject("Registration: We need you to confirm your e-mail address");
    //            helper.setText(text,true);
    //        }catch (Exception e){
    //            throw new RuntimeException("Failed to send confirmation email", e);
    //        }
    //
    //        sender.send(message);
}
