package org.telegram.telegramflow.actions;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.handlers.CallbackHandler;

public class HelpCallbackAction extends CallbackHandler {

    public static final String ACTION_ID = "HELP";

    @Override
    public void handle(Update update, String value) {
        System.out.println("Callback action with key HELP executed");
        throw new UnsupportedOperationException();
    }
}
