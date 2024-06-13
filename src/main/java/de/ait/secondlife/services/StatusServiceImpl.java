package de.ait.secondlife.services;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Status;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.StatusNotFoundException;
import de.ait.secondlife.repositories.StatusRepository;
import de.ait.secondlife.services.interfaces.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;

    @Override
    public Status getStatusById(Long id) {
        if (id == null) throw new IdIsNullException();
        return statusRepository.findById(id)
                .orElseThrow(()-> new StatusNotFoundException(id));
    }

    @Override
    public Status getByOfferStatus(OfferStatus offerStatus) {
        return statusRepository.findByName(offerStatus)
                .orElseThrow(()-> new StatusNotFoundException(offerStatus));
    }
}
