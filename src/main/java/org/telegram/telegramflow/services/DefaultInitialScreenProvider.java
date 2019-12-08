package org.telegram.telegramflow.services;

import org.telegram.telegramflow.dummy.InitialScreenProvider;
import org.telegram.telegramflow.objects.TelegramUser;

import javax.annotation.Nonnull;

public class DefaultInitialScreenProvider implements InitialScreenProvider {

    private final static String DEFAULT_SCREEN_ID = "default";

    @Nonnull
    @Override
    public String getInitialScreen(@Nonnull TelegramUser user) {
        return DEFAULT_SCREEN_ID;
    }
}
