package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.BidCreationDto;
import de.ait.secondlife.domain.entity.Bid;

import javax.security.auth.login.CredentialException;

public interface BidService {

    Bid getById(Long id);

    void save(BidCreationDto dto) throws CredentialException;
}
