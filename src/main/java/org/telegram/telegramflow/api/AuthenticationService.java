package org.telegram.telegramflow.api;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.services.SharePhoneAuthenticationService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface AuthenticationService {

    void setUserService(@Nonnull UserService userService);

    void setTelegramBot(@Nonnull TelegramBot telegramBot);

    void setMessageService(@Nonnull MessageService messageService);

    @Nonnull
    AuthenticationService setAfterAuthorized(@Nullable Consumer<TelegramUser> afterAuthorized);

    @Nonnull
    AuthenticationService setAfterRestricted(@Nullable Consumer<TelegramUser> afterRestricted);

    @Nonnull
    TelegramUser authorize(@Nonnull Update update) throws AuthenticationException;

    @Nonnull
    TelegramUser getCurrentUser();

    void end();

    void logout(@Nonnull TelegramUser user);
}
