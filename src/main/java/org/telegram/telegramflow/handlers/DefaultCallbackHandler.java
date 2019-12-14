package org.telegram.telegramflow.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.exceptions.CallbackException;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.services.defaults.CallbackService;

public class DefaultCallbackHandler implements UpdateHandler {

    private static CallbackService callbackService;

    public DefaultCallbackHandler() {
        if (callbackService == null) {
            callbackService = new CallbackService();
            try {
                callbackService.initialize();
            } catch (CallbackException e) {
                throw new IllegalStateException("Cannot initialize callback service", e);
            }
        }
    }

    @Override
    public void handle(Update update) throws ProcessException {
        try {
            callbackService.execute(update);
        } catch (CallbackException e) {
            throw new ProcessException("An error occurred while executing callback action", e);
        }
    }
}
