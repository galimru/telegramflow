package org.telegram.telegramflow.services.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.api.AuthenticationService;
import org.telegram.telegramflow.api.MessageService;
import org.telegram.telegramflow.api.TelegramBot;
import org.telegram.telegramflow.api.UserService;
import org.telegram.telegramflow.objects.TelegramUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractAuthenticationService implements AuthenticationService {

    private Logger logger = LoggerFactory.getLogger(AbstractAuthenticationService.class);

    protected final static ThreadLocal<TelegramUser> USER_HOLDER = new ThreadLocal<>();

    protected UserService userService;

    protected TelegramBot telegramBot;

    protected MessageService messageService;

    protected Consumer<TelegramUser> afterAuthorized = (user) -> {
        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(messageService.getMessage("authentication.authorizedMessage"))
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
                    .setText(messageService.getMessage("authentication.restrictedMessage"))
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending restricted message to user %s",
                    user.getUsername()), e);
        }
    };

    public AbstractAuthenticationService() {
    }

    public AbstractAuthenticationService(UserService userService, TelegramBot telegramBot, MessageService messageService) {
        this.userService = userService;
        this.telegramBot = telegramBot;
        this.messageService = messageService;
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
    public void setMessageService(@Nonnull MessageService messageService) {
        this.messageService = messageService;
    }

    @Nonnull
    public AbstractAuthenticationService setAfterAuthorized(@Nullable Consumer<TelegramUser> afterAuthorized) {
        this.afterAuthorized = afterAuthorized;
        return this;
    }

    @Nonnull
    public AbstractAuthenticationService setAfterRestricted(@Nullable Consumer<TelegramUser> afterRestricted) {
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
