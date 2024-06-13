package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class LocationNotFoundException extends ParameterNotFoundException{

    public LocationNotFoundException(Long id){

        super(String.format("Location with id <%s> not found", id));
    }
}
