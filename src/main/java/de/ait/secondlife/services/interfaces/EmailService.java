package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;

public interface EmailService {

    void createNotification(AuthenticatedUser authenticatedUser, NotificationType notificationType);

    void createNotification(AuthenticatedUser authenticatedUser, NotificationType notificationType, Long contextId) ;

    void sendPendingEmails();
}
