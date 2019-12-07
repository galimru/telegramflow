package org.telegram.telegramflow.exceptions;

public class ProcessException extends Exception {

    public ProcessException(Throwable cause) {
        super(cause);
    }

    public ProcessException(String message) {
        super(message);
    }

    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
