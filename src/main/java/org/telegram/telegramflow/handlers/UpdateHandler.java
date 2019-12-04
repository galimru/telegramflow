package org.telegram.telegramflow.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {

    void handle(Update update);
}
