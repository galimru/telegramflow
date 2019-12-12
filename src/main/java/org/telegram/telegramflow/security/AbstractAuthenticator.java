package org.telegram.telegramflow.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;
import org.telegram.telegramflow.services.MessageProvider;
import org.telegram.telegramflow.objects.TelegramUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractAuthenticator implements Authenticator {

    private Logger logger = LoggerFactory.getLogger(AbstractAuthenticator.class);

    protected final static ThreadLocal<TelegramUser> USER_HOLDER = new ThreadLocal<>();

    protected UserService userService;

    protected TelegramBot telegramBot;

    protected MessageProvider messageProvider;

    protected Consumer<TelegramUser> afterAuthorized = (user) -> {
        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(messageProvider.getMessage("authentication.authorizedMessage"))
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending authorized message to user %s",
                    user.getUsername()), e);
        }
    };

    protected Consumer<TelegramUser> afterRestricted = (user) -> {
        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(messageProvider.getMessage("authentication.restrictedMessage"))
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending restricted message to user %s",
                    user.getUsername()), e);
        }
    };

    public AbstractAuthenticator() {
    }

    public AbstractAuthenticator(UserService userService, TelegramBot telegramBot, MessageProvider messageProvider) {
        this.userService = userService;
        this.telegramBot = telegramBot;
        this.messageProvider = messageProvider;
    }

    @Override
    public void setUserService(@Nonnull UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setTelegramBot(@Nonnull TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void setMessageProvider(@Nonnull MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Nonnull
    public AbstractAuthenticator setAfterAuthorized(@Nullable Consumer<TelegramUser> afterAuthorized) {
        this.afterAuthorized = afterAuthorized;
        return this;
    }

    @Nonnull
    public AbstractAuthenticator setAfterRestricted(@Nullable Consumer<TelegramUser> afterRestricted) {
        this.afterRestricted = afterRestricted;
        return this;
    }

    @Nonnull
    @Override
    public TelegramUser getUser() {
        TelegramUser currentUser = USER_HOLDER.get();
        if (currentUser == null) {
            throw new IllegalStateException("There is no logged user");
        }
        return currentUser;
    }

    @Override
    public void end() {
        USER_HOLDER.remove();
    }

    @Override
    public void logout(@Nonnull TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        user.setRole(null);
        user.setActiveScreen(null);
        user.setAuthState(null);
        userService.save(user);

        end();

        logger.info("User {} logged out", user.getUsername());
    }

}
