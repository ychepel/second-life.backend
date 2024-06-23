package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.ConfirmationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode, Long> {

    ConfirmationCode findByUserId(Long userId);
}
