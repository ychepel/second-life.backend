package de.ait.secondlife.mailing;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;

public class EmailTemplateFactory {
    private Configuration mailConfig;

    public EmailTemplateFactory(Configuration mailConfig) {
        this.mailConfig = mailConfig;
    }

    public Template getTemplate(NotificationType notificationType) {
        try {
            return mailConfig.getTemplate(notificationType.getTemplateName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template: " + notificationType.getTemplateName(), e);
        }
    }
}
