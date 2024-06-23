package de.ait.secondlife.services.emails;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.domain.entity.Notification;
import de.ait.secondlife.services.interfaces.EmailTemplateService;
import freemarker.template.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailTemplateServiceFactory {

    private final ApplicationContext applicationContext;

    public EmailTemplateService createService(
            NotificationType notificationType,
            Configuration freemarkerConfiguration,
            Notification notification
    ) {
        Class<? extends EmailTemplateService> serviceClass = notificationType.getServiceClass();
        EmailTemplateService service = applicationContext.getAutowireCapableBeanFactory().createBean(serviceClass);
        service.setMailConfiguration(freemarkerConfiguration);
        service.setNotification(notification);

        return service;
    }
}
