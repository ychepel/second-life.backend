package de.ait.secondlife.constants;

public enum NotificationType {

    REGISTRATION_EMAIL("confirmation_registration_mail.ftlh");

    private final String templateName;


    NotificationType(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
