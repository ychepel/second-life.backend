package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Notification;
import de.ait.secondlife.exception_handling.exceptions.EmailTemplateException;
import freemarker.template.Configuration;

public interface EmailTemplateService {

    String getSubject();

    String getBody() throws EmailTemplateException;

    void setMailConfiguration(Configuration mailConfiguration);

    void setNotification(Notification notification);
}
