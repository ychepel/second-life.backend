package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.RejectionReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectionReasonRepository extends JpaRepository<RejectionReason, Long> {

}
