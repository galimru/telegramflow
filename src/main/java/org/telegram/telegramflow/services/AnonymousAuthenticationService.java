package org.telegram.telegramflow.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.dummy.AuthenticationService;
import org.telegram.telegramflow.dummy.MessageService;
import org.telegram.telegramflow.dummy.TelegramBot;
import org.telegram.telegramflow.dummy.UserService;
import org.telegram.telegramflow.objects.AuthState;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.utils.TelegramUtil;

import javax.annotation.Nonnull;
import java.util.Objects;

public class AnonymousAuthenticationService implements AuthenticationService {

    private Logger logger = LoggerFactory.getLogger(AnonymousAuthenticationService.class);

    private final static ThreadLocal<TelegramUser> CURRENT_USER = new ThreadLocal<>();

    private UserService userService;

    private TelegramBot telegramBot;

    private MessageService messageService;

    @Override
    public void setUserService(@Nonnull UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setTelegramBot(@Nonnull TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void setMessageService(@Nonnull MessageService messageService) {
        this.messageService = messageService;
    }

    @Nonnull
    @Override
    public TelegramUser authorize(@Nonnull Update update) throws AuthenticationException {
        Objects.requireNonNull(update, "update is null");

        TelegramUser user = retrieveUser(TelegramUtil.extractFrom(update));

        if (user.getAuthState() != AuthState.AUTHORIZED) {
            throw new AuthenticationException(String.format("User %s is not authorized", user.getUsername()));
        }

        CURRENT_USER.set(user);

        logger.info("User {} successfully authorized as anonymous", user.getUsername());

        return user;
    }

    @Nonnull
    @Override
    public TelegramUser getCurrentUser() {
        TelegramUser currentUser = CURRENT_USER.get();
        if (currentUser == null) {
            throw new IllegalStateException("Current user is not defined");
        }
        return currentUser;
    }

    @Override
    public void logout(@Nonnull TelegramUser user) {
        throw new UnsupportedOperationException();
    }

    private TelegramUser retrieveUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Objects.requireNonNull(telegramUser, "telegramUser is null");

        TelegramUser user = userService.find(String.valueOf(telegramUser.getId()));

        if (user == null) {
            user = userService.create();
            user.setUserId(String.valueOf(telegramUser.getId()));
            user.setUsername(telegramUser.getUserName());
            user.setFirstName(telegramUser.getFirstName());
            user.setLastName(telegramUser.getLastName());
            user.setAuthState(AuthState.AUTHORIZED);
            user.setRole(userService.retrieveRole(user));
            userService.save(user);
        }

        return user;
    }
}