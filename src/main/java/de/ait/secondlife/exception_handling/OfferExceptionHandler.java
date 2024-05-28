package de.ait.secondlife.exception_handling;

import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.exception_handling.exceptions.notFoundException.ParameterNotFoundException;
import de.ait.secondlife.exception_handling.exceptions.badRequestException.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public interface OfferExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    default ResponseEntity<ResponseMessageDto> handleException(BadRequestException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParameterNotFoundException.class)
    default ResponseEntity<ResponseMessageDto> handleException(ParameterNotFoundException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.NOT_FOUND);
    }
}
