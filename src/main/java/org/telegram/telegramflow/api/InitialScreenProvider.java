package org.telegram.telegramflow.api;

import org.telegram.telegramflow.objects.TelegramUser;

import javax.annotation.Nonnull;

public interface InitialScreenProvider {

    @Nonnull
    String getInitialScreen(@Nonnull TelegramUser user);
}
