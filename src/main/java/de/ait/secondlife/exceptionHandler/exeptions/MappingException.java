package de.ait.secondlife.exceptionHandler.exeptions;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.entity.Offer;

public class MappingException extends RuntimeException {

    public MappingException(OfferCreationDto dto) {

        super(String.format("Mapping  <%s>  is wrong", dto));
    }

    public MappingException(Offer offer) {

        super(String.format("Mapping  <%s>  is wrong", offer));
    }

    public MappingException() {

        super(String.format("Mapping  is wrong"));
    }
}
