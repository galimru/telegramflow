package org.telegram.telegramflow.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.TelegramFlow;
import org.telegram.telegramflow.exceptions.ProcessException;

public abstract class UpdateHandler {

    protected TelegramFlow telegramFlow;

    public void setTelegramFlow(TelegramFlow telegramFlow) {
        this.telegramFlow = telegramFlow;
    }

    public abstract void handle(Update update) throws ProcessException;
}
