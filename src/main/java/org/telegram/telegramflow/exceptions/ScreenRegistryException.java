package org.telegram.telegramflow.exceptions;

public class ScreenRegistryException extends Exception {

    public ScreenRegistryException(Throwable cause) {
        super(cause);
    }

    public ScreenRegistryException(String message) {
        super(message);
    }

    public ScreenRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
