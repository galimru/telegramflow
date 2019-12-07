package org.telegram.telegramflow.api;

import org.telegram.telegramflow.common.TelegramRole;
import org.telegram.telegramflow.common.TelegramUser;

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
