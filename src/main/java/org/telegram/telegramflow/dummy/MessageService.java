package org.telegram.telegramflow.dummy;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface MessageService {

    void initialize() throws IOException;

    @Nonnull
    String getMessage(@Nonnull String key);

    @Nonnull
    String formatMessage(@Nonnull String key, Object... args);
}
