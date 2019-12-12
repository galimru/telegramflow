package org.telegram.telegramflow.security;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.services.MessageProvider;
import org.telegram.telegramflow.objects.TelegramUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface Authenticator {

    void setUserService(@Nonnull UserService userService);

    void setTelegramBot(@Nonnull TelegramBot telegramBot);

    void setMessageProvider(@Nonnull MessageProvider messageProvider);

    @Nonnull
    Authenticator setAfterAuthorized(@Nullable Consumer<TelegramUser> afterAuthorized);

    @Nonnull
    Authenticator setAfterRestricted(@Nullable Consumer<TelegramUser> afterRestricted);

    @Nonnull
    TelegramUser authorize(@Nonnull Update update) throws AuthenticationException;

    @Nonnull
    TelegramUser getUser();

    void end();

    void logout(@Nonnull TelegramUser user);
}
