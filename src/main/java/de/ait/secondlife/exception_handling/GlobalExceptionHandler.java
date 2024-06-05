package de.ait.secondlife.exception_handling;

import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.exception_handling.dto.ValidationErrorDto;
import de.ait.secondlife.exception_handling.dto.ValidationErrorsDto;
import de.ait.secondlife.exception_handling.exceptions.DuplicateCategoryException;
import de.ait.secondlife.exception_handling.exceptions.DuplicateUserEmailException;
import de.ait.secondlife.exception_handling.exceptions.NoRightToChangeException;
import de.ait.secondlife.exception_handling.exceptions.UserSavingException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadRequestException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.ParameterNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserSavingException.class)
    public ResponseEntity<ResponseMessageDto> handleException(UserSavingException e) {
        //TODO save to logs stacktrace
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateUserEmailException.class)
    public ResponseEntity<ResponseMessageDto> handleException(DuplicateUserEmailException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseMessageDto> handleException(DataIntegrityViolationException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ResponseMessageDto> handleException(DuplicateCategoryException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<ResponseMessageDto> handleException(CredentialException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<ResponseMessageDto> handleException(LoginException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseMessageDto> handleException(BadRequestException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoRightToChangeException.class)
    public ResponseEntity<ResponseMessageDto> handleException(NoRightToChangeException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ParameterNotFoundException.class)
    public ResponseEntity<ResponseMessageDto> handleException(ParameterNotFoundException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseMessageDto> handleException(RuntimeException e) {
        return new ResponseEntity<>(new ResponseMessageDto("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseMessageDto> handleException(IllegalArgumentException e){
        return new ResponseEntity<>(new ResponseMessageDto("Invalid value"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorsDto> handleValidationException(MethodArgumentNotValidException e) {
        List<ValidationErrorDto> validationErrors = new ArrayList<>();
        List<ObjectError> errors = e.getBindingResult().getAllErrors();

        for (ObjectError error : errors) {
            FieldError fieldError = (FieldError) error;

            ValidationErrorDto errorDto = ValidationErrorDto.builder()
                    .field(fieldError.getField())
                    .message("Field "+fieldError.getDefaultMessage())
                    .build();

            if (fieldError.getRejectedValue() != null) {
                errorDto.setRejectedValue(fieldError.getRejectedValue().toString());
            }

            validationErrors.add(errorDto);
        }

        return ResponseEntity.badRequest()
                .body(ValidationErrorsDto.builder()
                        .errors(validationErrors)
                        .build());
    }

    private String parseExceptionMessage(String errorMessage) {
        Pattern pattern = Pattern.compile("messageTemplate='([^']*)'");
        Matcher matcher = pattern.matcher(errorMessage);
        return matcher.find() ? matcher.group(1) : "something went wrong";
    }


}
