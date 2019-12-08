package org.telegram.telegramflow.dummy;

import org.telegram.telegramflow.objects.TelegramRole;
import org.telegram.telegramflow.objects.TelegramUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface UserService {

    @Nonnull
    TelegramUser create();

    @Nullable
    TelegramUser find(@Nonnull String userId);

    @Nullable
    TelegramRole retrieveRole(@Nonnull TelegramUser user);

    void save(@Nonnull TelegramUser user);
}
