package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Status;
import de.ait.secondlife.exception_handling.exeptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exeptions.NameOfStatusIsNullException;
import de.ait.secondlife.exception_handling.exeptions.StatusNotFoundException;
import de.ait.secondlife.repositories.StatusRepository;
import de.ait.secondlife.services.interfaces.StatusSevice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusSeviceImpl implements StatusSevice {


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
