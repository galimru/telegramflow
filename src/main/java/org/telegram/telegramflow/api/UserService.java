package org.telegram.telegramflow.api;

import jdk.internal.jline.internal.Nullable;
import org.telegram.telegramflow.common.TelegramRole;
import org.telegram.telegramflow.common.TelegramUser;

import javax.annotation.Nonnull;

public interface UserService {

    @Nonnull
    TelegramUser create();

    @Nullable
    TelegramUser find(@Nonnull String userId);

    @Nullable
    TelegramRole retrieveRole(@Nonnull TelegramUser user);

    void save(@Nonnull TelegramUser user);
}
