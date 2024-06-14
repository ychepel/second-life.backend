package de.ait.secondlife.mailing;

import lombok.Getter;

@Getter
public enum NotificationType {

    REGISTRATION_EMAIL("confirmation_registration_mail.ftlh"),
    OFFER_VERIFICATION_EMAIL("offer_verification_mail.ftlh"),
    REJECTED_OFFER_EMAIL("rejected_offer_mail.ftlh"),
    OFFER_COMPLETED_DUE_TO_NO_BIDS("offer_completed_due_to_no_bids.ftlh");

    private final String templateName;

    NotificationType(String templateName) {
        this.templateName = templateName;
    }

}
