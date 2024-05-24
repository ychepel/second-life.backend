package de.ait.secondlife.exception_handling;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Response {

    private String message;
    private String additionalMessage;

    public Response(String message) {
        this.message = message;
    }

    public Response(String message, String additionalMessage) {
        this.message = message;
        this.additionalMessage = additionalMessage;
    }

    @Override
    public String toString() {
        return String.format("Response message: %s; Additional message: %s", message, additionalMessage);
    }
}
