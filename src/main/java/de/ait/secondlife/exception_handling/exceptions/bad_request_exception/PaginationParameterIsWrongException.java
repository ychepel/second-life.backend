package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;



public class PaginationParameterIsWrongException extends BadRequestException {

    public PaginationParameterIsWrongException(int page,int size, String sortBy) {

        super(String.format("Parameters of pagination is wrong :" +
                " page <%d>, size <%d>, sortBy <%s>", page,size,sortBy));
    }
}
