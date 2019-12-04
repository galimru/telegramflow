package org.telegram.telegramflow.api;

import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.common.TelegramUser;

public interface AuthenticationService {

    void setUserManager(UserManager userManager);

    void setTelegramBot(TelegramBot telegramBot);

    TelegramUser authorize(Update update) throws AuthenticationException;

    TelegramUser getCurrentUser();

    void logout(TelegramUser user);
}
