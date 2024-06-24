package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.ConfirmationCode;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.ConfirmationEmailCodeExpiredException;
import de.ait.secondlife.repositories.ConfirmationCodeRepository;
import de.ait.secondlife.services.interfaces.ConfirmationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation for managing confirmation codes.(Version 1.0)
 * This service provides methods for generating and retrieving confirmation codes
 * associated with user confirmation processes.
 *
 * <p>
 * This service interacts with the ConfirmationCodeRepository.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link ConfirmationEmailCodeExpiredException} - if the confirmation code has expired</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @version 1.0
 * @author: Second Life Team
 */
@Service
@RequiredArgsConstructor
public class ConfirmationServiceImpl implements ConfirmationService {

    private final ConfirmationCodeRepository repository;

    /**
     * Generates a confirmation code for the specified user.
     *
     * @param user the user for whom the confirmation code is generated.
     * @return the generated confirmation code.
     */
    @Override
    public String generateConfirmationCode(User user) {
        LocalDateTime expired = LocalDateTime.now().plusMinutes(10);
        String code = UUID.randomUUID().toString();
        ConfirmationCode entity = new ConfirmationCode(null, code,expired,user);
        repository.save(entity);
        return code;
    }

    /**
     * Retrieves the confirmation code associated with the specified user ID.
     *
     * @param userId the ID of the user for whom to retrieve the confirmation code.
     * @return the confirmation code associated with the user.
     * @throws ConfirmationEmailCodeExpiredException if the confirmation code has expired.
     */
    @Override
    public String getConfirmationCodeByUserId(Long userId) {
        ConfirmationCode confirmationCode = repository.findByUserId(userId);

        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime expiredDateTime = confirmationCode.getExpired();

        if (expiredDateTime == null || expiredDateTime.isBefore(localDateTime)) {
            throw new ConfirmationEmailCodeExpiredException(confirmationCode.getId(), userId);
        }
        return confirmationCode.getCode();
    }

    /**
     * Deletes all expired confirmation codes from the repository.
     * This method is transactional to ensure atomicity of the delete operation.
     */
    @Override
    @Transactional
    public void deleteAllExpired() {
        repository.deleteAllByExpiredLessThan(LocalDateTime.now());
    }
}
