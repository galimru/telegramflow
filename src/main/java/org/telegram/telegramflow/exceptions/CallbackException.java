package org.telegram.telegramflow.exceptions;

public class CallbackException extends Exception {

    public CallbackException(Throwable cause) {
        super(cause);
    }

    public CallbackException(String message) {
        super(message);
    }

    public CallbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
