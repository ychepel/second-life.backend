package de.ait.secondlife.exception_handling;

import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.exception_handling.exceptions.DuplicateUserEmailException;
import de.ait.secondlife.exception_handling.exceptions.UserSavingException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadRequestException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.ParameterNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserSavingException.class)
    public ResponseEntity<Response> handleException(UserSavingException e) {
        Throwable causeException = e.getCause();
        String additionalMessage = causeException == null ? null : parseExceptionMessage(causeException.getMessage());
        Response response = new Response(e.getMessage(), additionalMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateUserEmailException.class)
    public ResponseEntity<Response> handleException(DuplicateUserEmailException e) {
        Response response = new Response(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<Response> handleException(CredentialException e) {
        Response response = new Response(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<Response> handleException(LoginException e) {
        Response response = new Response(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseMessageDto> handleException(BadRequestException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParameterNotFoundException.class)
    public ResponseEntity<ResponseMessageDto> handleException(ParameterNotFoundException e) {
        return new ResponseEntity<>(new ResponseMessageDto(e.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseMessageDto> handleException(RuntimeException e) {
        return new ResponseEntity<>(new ResponseMessageDto("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String parseExceptionMessage(String errorMessage) {
        Pattern pattern = Pattern.compile("messageTemplate='([^']*)'");
        Matcher matcher = pattern.matcher(errorMessage);
        return matcher.find() ? matcher.group(1) : "something went wrong";
    }
}
