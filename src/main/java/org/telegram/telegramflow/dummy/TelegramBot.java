package org.telegram.telegramflow.dummy;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

public interface TelegramBot {

    <T extends Serializable, M extends BotApiMethod<T>> T execute(M method) throws TelegramApiException;
}
