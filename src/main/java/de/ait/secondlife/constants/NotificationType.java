package de.ait.secondlife.constants;

import de.ait.secondlife.services.emails.OfferVerification;
import de.ait.secondlife.services.emails.Registration;
import de.ait.secondlife.services.emails.RejectedOffer;
import de.ait.secondlife.services.interfaces.EmailTemplateService;
import lombok.Getter;

@Getter
public enum NotificationType {

    REGISTRATION_EMAIL(Registration.class),
    OFFER_VERIFICATION_EMAIL(OfferVerification.class),
    REJECTED_OFFER_EMAIL(RejectedOffer.class);

    private final Class<? extends EmailTemplateService> serviceClass;

    NotificationType(Class<? extends EmailTemplateService> serviceClass) {
        this.serviceClass = serviceClass;
    }
}
