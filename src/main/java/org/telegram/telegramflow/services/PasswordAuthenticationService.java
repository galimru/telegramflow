package org.telegram.telegramflow.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.api.AuthenticationService;
import org.telegram.telegramflow.api.MessageService;
import org.telegram.telegramflow.api.TelegramBot;
import org.telegram.telegramflow.api.UserService;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.objects.AuthState;
import org.telegram.telegramflow.objects.TelegramRole;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.utils.TelegramUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class PasswordAuthenticationService implements AuthenticationService {

    private Logger logger = LoggerFactory.getLogger(PasswordAuthenticationService.class);

    private final static ThreadLocal<TelegramUser> CURRENT_USER = new ThreadLocal<>();

    private UserService userService;

    private TelegramBot telegramBot;

    private MessageService messageService;

    private Map<String, String> loginMap = new ConcurrentHashMap<>();

    private Consumer<TelegramUser> afterAuthorized = (user) -> {
        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(messageService.getMessage("authentication.authorizedMessage")));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending authorized message to user %s",
                    user.getUsername()), e);
        }
    };

    private Consumer<TelegramUser> afterRestricted = (user) -> {
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

    public PasswordAuthenticationService() {
    }

    public PasswordAuthenticationService(UserService userService, TelegramBot telegramBot, MessageService messageService) {
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
    public PasswordAuthenticationService setAfterAuthorized(@Nullable Consumer<TelegramUser> afterAuthorized) {
        this.afterAuthorized = afterAuthorized;
        return this;
    }

    @Nonnull
    public PasswordAuthenticationService setAfterRestricted(@Nullable Consumer<TelegramUser> afterRestricted) {
        this.afterRestricted = afterRestricted;
        return this;
    }

    @Nonnull
    @Override
    public TelegramUser authorize(@Nonnull Update update) throws AuthenticationException {
        Objects.requireNonNull(update, "update is null");

        TelegramUser user = retrieveUser(TelegramUtil.extractFrom(update));

        if (user.getAuthState() == null) {
            String text = TelegramUtil.extractText(update);

            if (messageService.getMessage("authentication.authorizeButton").equals(text)) {
                startAuthorizationProcess(user);
                sendLoginRequest(user);
                throw new AuthenticationException(String.format("Authorization process require login for user %s",
                        user.getUsername()));
            }

            sendAuthorizationRequest(user, messageService.getMessage("authentication.authorizeMessage"));
            throw new AuthenticationException(String.format("Authorization process required for user %s",
                    user.getUsername()));
        } else if (user.getAuthState() == AuthState.AUTHORIZATION) {
            String text = TelegramUtil.extractText(update);

            if (text == null) {
                sendInvalidMessage(user);
                throw new AuthenticationException(String.format("User %s sent invalid authorization message",
                        user.getUsername()));
            }

            String login = loginMap.get(user.getUserId());
            String password;
            if (login == null) {
                loginMap.put(user.getUserId(), text);
                sendPasswordRequest(user);
                throw new AuthenticationException(String.format("Authorization process require password for user %s",
                        user.getUsername()));
            } else {
                password = text;
                loginMap.remove(user.getUserId());
            }

            try {
                TelegramRole role = login(new Credentials(user, login, password));
                user.setRole(role);
                completeAuthorizationProcess(user);
            } catch (AuthenticationException e) {
                interruptAuthorizationProcess(user);
                throw e;
            }
        }

        if (user.getRole() == null) {
            sendAuthorizationRequest(user, messageService.getMessage("authentication.authorizeMessage"));
            throw new AuthenticationException(String.format("User %s doesn't have role", user.getUsername()));
        }

        if (user.getAuthState() != AuthState.AUTHORIZED) {
            throw new AuthenticationException(String.format("User %s is not authorized", user.getUsername()));
        }

        CURRENT_USER.set(user);

        logger.info("User {} successfully authorized with role {}", user.getUsername(), user.getRole());

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
    public void end() {
        CURRENT_USER.remove();
    }

    @Override
    public void logout(@Nonnull TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        CURRENT_USER.remove();
        user.setRole(null);
        user.setActiveScreen(null);
        user.setAuthState(null);
        userService.save(user);
        logger.info("User {} logged out", user.getUsername());
        startAuthorizationProcess(user);
    }

    @Nullable
    protected abstract TelegramRole login(@Nonnull Credentials credentials) throws AuthenticationException;

    private TelegramUser retrieveUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Objects.requireNonNull(telegramUser, "telegramUser is null");

        TelegramUser user = userService.find(String.valueOf(telegramUser.getId()));

        if (user == null) {
            user = userService.create();
            user.setUserId(String.valueOf(telegramUser.getId()));
            user.setUsername(telegramUser.getUserName());
            user.setFirstName(telegramUser.getFirstName());
            user.setLastName(telegramUser.getLastName());
            userService.save(user);
        }

        return user;
    }

    private void startAuthorizationProcess(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        user.setAuthState(AuthState.AUTHORIZATION);
        userService.save(user);
    }

    private void sendAuthorizationRequest(TelegramUser user, String message) {
        Objects.requireNonNull(user, "user is null");

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton()
                .setText(messageService.getMessage("authentication.authorizeButton")));
        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(message)
                    .setReplyMarkup(new ReplyKeyboardMarkup()
                            .setResizeKeyboard(true)
                            .setKeyboard(Collections.singletonList(keyboardRow))));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending authorization request to user %s",
                    user.getUsername()), e);
        }
    }

    private void sendLoginRequest(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(messageService.getMessage("authentication.loginMessage"))
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending login request to user %s",
                    user.getUsername()), e);
        }
    }

    private void sendPasswordRequest(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(messageService.getMessage("authentication.passwordMessage"))
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending password request to user %s",
                    user.getUsername()), e);
        }
    }

    private void sendInvalidMessage(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(messageService.getMessage("authentication.invalidMessage")));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending invalid message to user %s",
                    user.getUsername()), e);
        }
    }

    private void completeAuthorizationProcess(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        user.setAuthState(AuthState.AUTHORIZED);
        if (user.getRole() == null) {
            user.setRole(userService.retrieveRole(user));
        }
        userService.save(user);
        if (afterAuthorized != null) {
            afterAuthorized.accept(user);
        }
    }

    private void interruptAuthorizationProcess(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        user.setAuthState(null);
        user.setRole(null);
        userService.save(user);
        if (afterRestricted != null) {
            afterRestricted.accept(user);
        }
    }

    public static class Credentials {

        private TelegramUser user;
        private String login;
        private String password;

        public Credentials(TelegramUser user, String login, String password) {
            Objects.requireNonNull(user, "user is null");
            Objects.requireNonNull(login, "login is null");
            Objects.requireNonNull(password, "password is null");

            this.user = user;
            this.login = login;
            this.password = password;
        }

        @Nonnull
        public TelegramUser getUser() {
            return user;
        }

        @Nonnull
        public String getLogin() {
            return login;
        }

        @Nonnull
        public String getPassword() {
            return password;
        }
    }

}
