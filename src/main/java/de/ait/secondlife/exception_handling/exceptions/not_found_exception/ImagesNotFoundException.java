package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class ImagesNotFoundException extends ParameterNotFoundException {
    public ImagesNotFoundException(String type, Long id) {

        super(String.format("Images for entity type : <%s> id : <%d> not found",type, id));
    }
}
