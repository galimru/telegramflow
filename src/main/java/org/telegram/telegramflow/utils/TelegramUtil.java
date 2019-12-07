package org.telegram.telegramflow.utils;

import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class TelegramUtil {

    @Nullable
    public static User extractFrom(@Nonnull Update update) {
        Objects.requireNonNull(update, "update is null");

        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        }
        if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        }
        return null;
    }

    @Nullable
    public static Contact extractContact(@Nonnull Update update) {
        Objects.requireNonNull(update, "update is null");

        if (update.hasMessage() && update.getMessage().hasContact()) {
            return update.getMessage().getContact();
        }
        return null;
    }

    @Nonnull
    public static String normalizePhone(@Nonnull String phone) {
        Objects.requireNonNull(phone, "phone is null");
        return phone.replaceAll("\\+|\\s", "");
    }
}
