package de.ait.secondlife.exceptionHandler.exeptions;

public class PageableIsNullException extends RuntimeException{
    public PageableIsNullException() {

        super("Pageable is null");
    }
}
