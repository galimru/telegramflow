package org.telegram.telegramflow.services.defaults;

import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.services.InitialScreenProvider;

import javax.annotation.Nonnull;

public class DefaultInitialScreenProvider implements InitialScreenProvider {

    private final static String DEFAULT_SCREEN_ID = "default";

    @Nonnull
    @Override
    public String getInitialScreen(@Nonnull TelegramUser user) {
        return DEFAULT_SCREEN_ID;
    }
}
