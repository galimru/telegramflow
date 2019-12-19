package org.telegram.telegramflow.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.services.MessageProvider;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;
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

public abstract class PasswordAuthenticator extends AbstractAuthenticator {

    private Logger logger = LoggerFactory.getLogger(PasswordAuthenticator.class);

    private Map<String, String> loginMap = new ConcurrentHashMap<>();

    public PasswordAuthenticator() {
    }

    public PasswordAuthenticator(UserService userService, TelegramBot telegramBot, MessageProvider messages) {
        super(userService, telegramBot, messages);
    }

    @Nonnull
    @Override
    public TelegramUser authorize(@Nonnull Update update) throws AuthenticationException {
        Objects.requireNonNull(update, "update is null");

        TelegramUser user = retrieveUser(TelegramUtil.extractFrom(update));

        if (user.getAuthState() == null) {
            String text = TelegramUtil.extractText(update);

            if (messageProvider.getMessage("authentication.authorizeButton").equals(text)) {
                startAuthorizationProcess(user);
                sendLoginRequest(user);
                throw new AuthenticationException(String.format("Authorization process require login for user %s",
                        user.getUsername()));
            }

            sendAuthorizationRequest(user, messageProvider.getMessage("authentication.authorizeMessage"));
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
            sendAuthorizationRequest(user, messageProvider.getMessage("authentication.authorizeMessage"));
            throw new AuthenticationException(String.format("User %s doesn't have role", user.getUsername()));
        }

        if (user.getAuthState() != AuthState.AUTHORIZED) {
            throw new AuthenticationException(String.format("User %s is not authorized", user.getUsername()));
        }

        USER_HOLDER.set(user);

        logger.info("User {} successfully authorized with role {}", user.getUsername(), user.getRole());

        return user;
    }

    @Override
    public void logout(@Nonnull TelegramUser user) {
        super.logout(user);
        startAuthorizationProcess(user);
    }

    @Nullable
    protected abstract TelegramRole login(@Nonnull Credentials credentials) throws AuthenticationException;

    protected TelegramUser retrieveUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
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

    protected void startAuthorizationProcess(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        user.setAuthState(AuthState.AUTHORIZATION);
        userService.save(user);
    }

    protected void sendAuthorizationRequest(TelegramUser user, String message) {
        Objects.requireNonNull(user, "user is null");

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton()
                .setText(messageProvider.getMessage("authentication.authorizeButton")));
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

    protected void sendLoginRequest(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(messageProvider.getMessage("authentication.loginMessage"))
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending login request to user %s",
                    user.getUsername()), e);
        }
    }

    protected void sendPasswordRequest(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(messageProvider.getMessage("authentication.passwordMessage"))
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending password request to user %s",
                    user.getUsername()), e);
        }
    }

    protected void sendInvalidMessage(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(messageProvider.getMessage("authentication.invalidMessage")));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending invalid message to user %s",
                    user.getUsername()), e);
        }
    }

    protected void completeAuthorizationProcess(TelegramUser user) {
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

    protected void interruptAuthorizationProcess(TelegramUser user) {
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
