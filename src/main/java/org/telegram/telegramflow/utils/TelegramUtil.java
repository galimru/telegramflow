package org.telegram.telegramflow.utils;

import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class TelegramUtil {

    public static User extractFrom(Update update) {
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

    public static Contact extractContact(Update update) {
        if (update.hasMessage() && update.getMessage().hasContact()) {
            return update.getMessage().getContact();
        }
        return null;
    }

    public static String normalizePhone(String phone) {
        return phone.replaceAll("\\+|\\s", "");
    }
}
