package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Status;

public interface StatusSevice {

    Status getStatusById(Long id);
    Status getStatusByName(String name);
}
