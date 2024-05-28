package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Status;
import de.ait.secondlife.exception_handling.exceptions.badRequestException.isNullExceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.badRequestException.isNullExceptions.NameOfStatusIsNullException;
import de.ait.secondlife.exception_handling.exceptions.notFoundException.StatusNotFoundException;
import de.ait.secondlife.repositories.StatusRepository;
import de.ait.secondlife.services.interfaces.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusSeviceImpl implements StatusService {


    private final StatusRepository statusRepository;

    @Override
    public Status getStatusById(Long id) {
        if (id == null) throw new IdIsNullException();
        return statusRepository.findById(id)
                .orElseThrow(()-> new StatusNotFoundException(id));
    }

    @Override
    public Status getStatusByName(String name) {
        if (name == null) throw new NameOfStatusIsNullException();
        return statusRepository.findByName(name)
                .orElseThrow(()-> new StatusNotFoundException(name));
    }
}
