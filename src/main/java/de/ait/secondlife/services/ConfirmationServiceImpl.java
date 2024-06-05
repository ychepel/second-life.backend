package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.ConfirmationCode;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.repositories.ConfirmationCodeRepository;
import de.ait.secondlife.services.interfaces.ConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmationServiceImpl implements ConfirmationService {

    private final ConfirmationCodeRepository repository;
    @Override
    public String generateConfirmationCode(User user) {
        LocalDateTime expired = LocalDateTime.now().plusMinutes(10);
        String code = UUID.randomUUID().toString();
        ConfirmationCode entity = new ConfirmationCode(null, code,expired,user);
        repository.save(entity);
        return code;
    }
}
