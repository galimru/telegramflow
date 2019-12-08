package org.telegram.telegramflow.api;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.exceptions.AuthenticationException;

import javax.annotation.Nonnull;

public interface AuthenticationService {

    void setUserService(@Nonnull UserService userService);

    void setTelegramBot(@Nonnull TelegramBot telegramBot);

    void setMessageService(@Nonnull MessageService messageService);

    @Nonnull
    TelegramUser authorize(@Nonnull Update update) throws AuthenticationException;

    @Nonnull
    TelegramUser getCurrentUser();

    void logout(@Nonnull TelegramUser user);
}
