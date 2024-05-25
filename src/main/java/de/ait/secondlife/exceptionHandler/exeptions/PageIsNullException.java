package de.ait.secondlife.exceptionHandler.exeptions;

public class PageIsNullException extends RuntimeException{
    public PageIsNullException() {

        super("Page is null");
    }
}
