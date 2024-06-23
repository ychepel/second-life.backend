package de.ait.secondlife.exception_handling.exceptions;

public class EmailTemplateException extends RuntimeException {

    public EmailTemplateException() {
    }

    public EmailTemplateException(String message) {
        super(message);
    }

    public EmailTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailTemplateException(Throwable cause) {
        super("Exception in email template", cause);
    }
}