package org.telegram.telegramflow.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackAction {

    void execute(Update update, String value);
}
