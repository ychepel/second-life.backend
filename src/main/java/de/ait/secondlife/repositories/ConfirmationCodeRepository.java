package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.ConfirmationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode, Long> {

    ConfirmationCode findByUserId(Long userId);

    void deleteAllByExpiredLessThan(LocalDateTime dateTime);
}
