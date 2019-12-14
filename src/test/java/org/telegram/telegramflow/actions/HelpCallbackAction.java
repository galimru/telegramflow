package org.telegram.telegramflow.actions;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.handlers.CallbackAction;

public class HelpCallbackAction implements CallbackAction {

    public static final String KEY = "HELP";

    @Override
    public void execute(Update update, String value) {
        System.out.println("Callback action with key HELP executed");
        throw new UnsupportedOperationException();
    }
}
