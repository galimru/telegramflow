package org.telegram.telegramflow.api;

import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.common.User;

public interface AuthenticationService {

    void setUserService(UserService userService);

    void setTelegramService(TelegramService telegramService);

    User authorize(Update update) throws AuthenticationException;

    User getCurrentUser();

    void logout(User user);
}
