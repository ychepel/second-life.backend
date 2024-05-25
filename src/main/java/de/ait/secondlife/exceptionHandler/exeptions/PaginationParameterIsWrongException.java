package de.ait.secondlife.exceptionHandler.exeptions;

import org.springframework.web.bind.annotation.RequestParam;

public class PaginationParameterIsWrongException extends RuntimeException {

    public PaginationParameterIsWrongException(int page,int size, String sortBy) {

        super(String.format("Parameters of pagination is wrong :" +
                " page <%d>, size <%d>, sortBy <%s>", page,size,sortBy));
    }
}
