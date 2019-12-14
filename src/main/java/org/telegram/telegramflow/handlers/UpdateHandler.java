package org.telegram.telegramflow.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.exceptions.ProcessException;

public interface UpdateHandler {

    void handle(Update update) throws ProcessException;
}
