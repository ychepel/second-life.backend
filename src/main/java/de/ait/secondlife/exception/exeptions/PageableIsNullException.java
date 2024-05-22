package de.ait.secondlife.exception.exeptions;

public class PageableIsNullException extends RuntimeException{
    public PageableIsNullException() {

        super("Pageable is null");
    }
}
