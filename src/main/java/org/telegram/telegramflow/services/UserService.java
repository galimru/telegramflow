package org.telegram.telegramflow.services;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.objects.TelegramRole;
import org.telegram.telegramflow.objects.TelegramUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface UserService {

    @Nonnull
    TelegramUser create(Update update);

    @Nullable
    TelegramUser find(@Nonnull String userId);

    @Nullable
    TelegramRole retrieveRole(@Nonnull TelegramUser user);

    void save(@Nonnull TelegramUser user);
}
