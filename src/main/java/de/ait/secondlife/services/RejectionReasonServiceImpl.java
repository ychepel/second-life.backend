package de.ait.secondlife.services;

import de.ait.secondlife.domain.dto.RejectionReasonsDto;
import de.ait.secondlife.domain.entity.RejectionReason;
import de.ait.secondlife.repositories.RejectionReasonRepository;
import de.ait.secondlife.services.interfaces.RejectionReasonService;
import de.ait.secondlife.services.mapping.RejectionReasonMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RejectionReasonServiceImpl implements RejectionReasonService {

    private final RejectionReasonRepository reasonRepository;
    private final RejectionReasonMappingService mappingService;

    @Override
    public RejectionReason getById(Long id) {
        return reasonRepository.findById(id).orElse(null);
    }

    @Override
    public RejectionReasonsDto getAll() {
        RejectionReasonsDto reasonsDto = new RejectionReasonsDto();
        reasonsDto.setReasons(reasonRepository.findAll()
                .stream()
                .map(mappingService::toDto)
                .toList());

        return reasonsDto;
    }

}
