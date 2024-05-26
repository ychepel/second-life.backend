package de.ait.secondlife.exception_handling.exeptions;

public class StatusNotFoundException extends RuntimeException {
    public StatusNotFoundException(Long id) {

        super(String.format("Status with id <%d> not found", id));
    }
    public StatusNotFoundException(String name) {

        super(String.format("Status with name <%s> not found", name));
    }
}
