package org.telegram.telegramflow.api;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class DummyTelegramService implements TelegramService {
    @Override
    public void execute(BotApiMethod method) throws TelegramApiException {
        // ignore
    }
}
