package org.telegram.telegramflow.services;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.TelegramFlow;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.handlers.CallbackHandler;
import org.telegram.telegramflow.handlers.UpdateHandler;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CallbackService {

    private final static String DELIMITER = "#";

    private Map<String, CallbackHandler> actions = new ConcurrentHashMap<>();

    public CallbackService register(String actionId, CallbackHandler action) {
        Objects.requireNonNull(actionId, "actionId is null");
        Objects.requireNonNull(action, "action is null");

        actions.put(actionId, action);
        return this;
    }

    @Nonnull
    public CallbackHandler get(@Nonnull String actionId) throws ProcessException {
        Objects.requireNonNull(actions, "actionId is null");

        CallbackHandler action = actions.get(actionId);
        if (action == null) {
            throw new ProcessException(String.format("Callback action '%s' is not registered", actionId));
        }
        return action;
    }

    public UpdateHandler createHandler() {
        return new UpdateHandler() {
            @Override
            public void setTelegramFlow(TelegramFlow telegramFlow) {
                actions.values().forEach(action -> {
                    action.setTelegramFlow(telegramFlow);
                });
            }

            @Override
            public void handle(Update update) throws ProcessException {
                if (!update.hasCallbackQuery()) {
                    throw new ProcessException("Update doesn't have callback query");
                }

                String data = update.getCallbackQuery().getData();
                String[] tokens = data.split(DELIMITER);

                String actionId = tokens[0];
                String value = tokens.length == 2 ? tokens[1] : null;

                get(actionId).handle(update, value);
            }
        };
    }
}
