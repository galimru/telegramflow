package org.telegram.telegramflow.api;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramService {

    void execute(BotApiMethod method) throws TelegramApiException;
}
