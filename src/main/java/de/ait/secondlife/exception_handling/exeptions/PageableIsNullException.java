package de.ait.secondlife.exception_handling.exeptions;

public class PageableIsNullException extends RuntimeException{
    public PageableIsNullException() {

        super("Pageable is null");
    }
}
