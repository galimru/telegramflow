package org.telegram.telegramflow.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.TelegramFlow;
import org.telegram.telegramflow.exceptions.ProcessException;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CallbackService {

    private final static String DELIMITER = "#";

    private Map<String, CallbackAction> actions = new ConcurrentHashMap<>();

    private TelegramFlow telegramFlow;

    public CallbackService register(String key, CallbackAction action) {
        Objects.requireNonNull(key, "key is null");
        Objects.requireNonNull(action, "action is null");

        actions.put(key, action);
        return this;
    }

    @Nonnull
    public CallbackAction get(@Nonnull String key) throws ProcessException {
        Objects.requireNonNull(actions, "key is null");

        CallbackAction action = actions.get(key);
        if (action == null) {
            throw new ProcessException(String.format("Callback action '%s' is not registered", key));
        }
        return action;
    }

    public CallbackHandler createHandler() {
        return new CallbackHandler();
    }

    public class CallbackHandler extends UpdateHandler {

        @Override
        public void handle(Update update) throws ProcessException {
            if (!update.hasCallbackQuery()) {
                throw new ProcessException("Update doesn't have callback query");
            }

            String data = update.getCallbackQuery().getData();
            String[] tokens = data.split(DELIMITER);

            String key = tokens[0];
            String value = tokens.length == 2 ? tokens[1] : null;

            get(key).execute(update, value);
        }
    }
}
