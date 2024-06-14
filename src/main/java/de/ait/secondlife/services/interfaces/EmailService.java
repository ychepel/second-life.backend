package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.mailing.NotificationType;
import de.ait.secondlife.domain.entity.User;

public interface EmailService {

    void sendConfirmationEmailToFinishRegistration(User user);

    void createNotification(User user, NotificationType notificationType);

    void sendPendingEmail();

    void sendEmail(User user, NotificationType notificationType, Offer offer);
}
