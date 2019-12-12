package org.telegram.telegramflow.services;

import javax.annotation.Nonnull;

public interface MessageProvider {

    @Nonnull
    String getMessage(@Nonnull String key);

    @Nonnull
    String formatMessage(@Nonnull String key, Object... args);
}
