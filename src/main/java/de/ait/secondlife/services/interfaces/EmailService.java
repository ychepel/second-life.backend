package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.domain.entity.User;

public interface EmailService {

    void sendConfirmationEmailToFinishRegistration(User user);

    void createNotification(User user, NotificationType notificationType);

    void sendPendingEmail();
}
