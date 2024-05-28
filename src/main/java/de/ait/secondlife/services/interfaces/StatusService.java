package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Status;

public interface StatusService {

    Status getStatusById(Long id);
    Status getStatusByName(String name);
}
