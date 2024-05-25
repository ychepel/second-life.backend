package de.ait.secondlife.exceptionHandler.exeptions;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.entity.Offer;

public class MappingException extends RuntimeException {

    public MappingException(OfferCreationDto dto, String message) {

        super(String.format("Mapping  <%s>  is wrong. %s", dto, message));
    }

    public MappingException(Offer offer, String message) {

        super(String.format("Mapping  <%s>  is wrong. %s", offer, message));
    }

    public MappingException(String message) {

        super(String.format("Mapping  is wrong. %s", message));
    }
}
