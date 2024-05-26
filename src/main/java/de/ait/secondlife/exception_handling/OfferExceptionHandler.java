package de.ait.secondlife.exception_handling;

import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.exception_handling.exeptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public interface OfferExceptionHandler {
    @ExceptionHandler(CreateOfferConstraintViolationException.class)
    default ResponseEntity<ResponseMessageDto> handleException(CreateOfferConstraintViolationException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DataBaseException.class)
    default ResponseEntity<ResponseMessageDto> handleException(DataBaseException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(IdIsNullException.class)
    default ResponseEntity<ResponseMessageDto> handleException(IdIsNullException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MappingException.class)
    default ResponseEntity<ResponseMessageDto> handleException(MappingException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NameOfStatusIsNullException.class)
    default ResponseEntity<ResponseMessageDto> handleException(NameOfStatusIsNullException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OfferCreationDtoIsNullException.class)
    default ResponseEntity<ResponseMessageDto> handleException(OfferCreationDtoIsNullException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OfferNotFoundException.class)
    default ResponseEntity<ResponseMessageDto> handleException(OfferNotFoundException e){
        return new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(OffersNotFoundException.class)
    default ResponseEntity<ResponseMessageDto> handleException(OffersNotFoundException e){
        return new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(OfferUpdateDtoIsNullException.class)
    default ResponseEntity<ResponseMessageDto> handleException(OfferUpdateDtoIsNullException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(PageableIsNullException.class)
    default ResponseEntity<ResponseMessageDto> handleException(PageableIsNullException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(PaginationParameterIsWrongException.class)
    default ResponseEntity<ResponseMessageDto> handleException(PaginationParameterIsWrongException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(StatusNotFoundException.class)
    default ResponseEntity<ResponseMessageDto> handleException(StatusNotFoundException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(WrongAuctionParameterException.class)
    default ResponseEntity<ResponseMessageDto> handleException(WrongAuctionParameterException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ValidationException.class)
    default ResponseEntity<ResponseMessageDto> handleException(ValidationException e){
        return  new  ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
