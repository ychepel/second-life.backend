package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.User;

public interface ConfirmationService {

    String generateConfirmationCode(User user);

    String getConfirmationCodeByUserId(Long userId);
}
