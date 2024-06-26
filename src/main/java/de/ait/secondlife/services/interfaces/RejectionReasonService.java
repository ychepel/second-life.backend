package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.RejectionReasonsDto;
import de.ait.secondlife.domain.entity.RejectionReason;

public interface RejectionReasonService {

    RejectionReason getById(Long id);

    RejectionReasonsDto getAll();
}
