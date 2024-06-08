package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.RejectionReason;
import de.ait.secondlife.repositories.RejectionReasonRepository;
import de.ait.secondlife.services.interfaces.RejectionReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RejectionReasonServiceImpl implements RejectionReasonService {

    private final RejectionReasonRepository reasonRepository;

    @Override
    public RejectionReason getById(Long id) {
        return reasonRepository.findById(id).orElse(null);
    }
}
