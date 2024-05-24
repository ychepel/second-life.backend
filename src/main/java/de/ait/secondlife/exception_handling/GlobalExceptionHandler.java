package de.ait.secondlife.exception_handling;

import de.ait.secondlife.exception_handling.exceptions.DuplicateUserEmailException;
import de.ait.secondlife.exception_handling.exceptions.UserSavingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

    private String parseExceptionMessage(String errorMessage) {
        Pattern pattern = Pattern.compile("messageTemplate='([^']*)'");
        Matcher matcher = pattern.matcher(errorMessage);
        return matcher.find() ? matcher.group(1) : "something went wrong";
    }
}
