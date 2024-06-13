package de.ait.secondlife.repositories;


import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {

    Optional<Status> findByName(OfferStatus offerStatus);
}
