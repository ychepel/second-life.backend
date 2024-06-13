package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class FileNotFoundExecption extends ParameterNotFoundException{
    public FileNotFoundExecption(String fileName) {
        super(String.format("File <%s> not found", fileName));
    }
}
