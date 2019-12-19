package org.telegram.telegramflow.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.services.MessageProvider;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.objects.AuthState;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.utils.TelegramUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class AnonymousAuthenticator extends AbstractAuthenticator {

    private Logger logger = LoggerFactory.getLogger(AnonymousAuthenticator.class);

    public AnonymousAuthenticator() {
    }

    public AnonymousAuthenticator(UserService userService, TelegramBot telegramBot, MessageProvider messages) {
        super(userService, telegramBot, messages);
    }

    @Nonnull
    @Override
    public AbstractAuthenticator setAfterAuthorized(@Nullable Consumer<TelegramUser> afterAuthorized) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public AbstractAuthenticator setAfterRestricted(@Nullable Consumer<TelegramUser> afterRestricted) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public TelegramUser authorize(@Nonnull Update update) throws AuthenticationException {
        Objects.requireNonNull(update, "update is null");

        TelegramUser user = retrieveUser(TelegramUtil.extractFrom(update));

        if (user.getAuthState() != AuthState.AUTHORIZED) {
            throw new AuthenticationException(String.format("User %s is not authorized", user.getUsername()));
        }

        USER_HOLDER.set(user);

        logger.info("User {} successfully authorized as anonymous", user.getUsername());

        return user;
    }

    protected TelegramUser retrieveUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
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