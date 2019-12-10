package org.telegram.telegramflow.services;

import org.telegram.telegramflow.api.AuthenticationService;
import org.telegram.telegramflow.api.MessageService;
import org.telegram.telegramflow.api.TelegramBot;
import org.telegram.telegramflow.api.UserService;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.objects.AuthState;
import org.telegram.telegramflow.objects.TelegramRole;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.utils.TelegramUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

public class SharePhoneAuthenticationService implements AuthenticationService {

    private Logger logger = LoggerFactory.getLogger(SharePhoneAuthenticationService.class);

    private final static ThreadLocal<TelegramUser> CURRENT_USER = new ThreadLocal<>();

    private UserService userService;

    private TelegramBot telegramBot;

    private MessageService messageService;

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

    public SharePhoneAuthenticationService() {
    }

    public SharePhoneAuthenticationService(UserService userService, TelegramBot telegramBot, MessageService messageService) {
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
    @Override
    public SharePhoneAuthenticationService setAfterAuthorized(@Nullable Consumer<TelegramUser> afterAuthorized) {
        this.afterAuthorized = afterAuthorized;
        return this;
    }

    @Nonnull
    @Override
    public SharePhoneAuthenticationService setAfterRestricted(@Nullable Consumer<TelegramUser> afterRestricted) {
        this.afterRestricted = afterRestricted;
        return this;
    }

    @Nonnull
    @Override
    public TelegramUser authorize(@Nonnull Update update) throws AuthenticationException {
        Objects.requireNonNull(update, "update is null");

        TelegramUser user = retrieveUser(TelegramUtil.extractFrom(update));

        if (user.getAuthState() == null) {
            startAuthorizationProcess(user);
            throw new AuthenticationException(String.format("Authorization process required for user %s",
                    user.getUsername()));
        }

        if (user.getAuthState() == AuthState.AUTHORIZATION) {
            Contact contact = TelegramUtil.extractContact(update);

            if (contact == null) {
                sendAuthorizationRequest(user);
                throw new AuthenticationException(String.format("User %s sent invalid authorization message",
                        user.getUsername()));
            }

            if (!user.getUserId().equals(String.valueOf(contact.getUserID()))) {
                sendAuthorizationRequest(user);
                throw new AuthenticationException(String.format("Contact %s doesn't belong to user %s",
                        contact.getPhoneNumber(), user.getUsername()));
            }

            String normalizedPhone = TelegramUtil.normalizePhone(contact.getPhoneNumber());
            user.setPhone(normalizedPhone);

            assignRole(user);
            completeAuthorizationProcess(user);
        }

        if (user.getRole() == null) {
            sendAuthorizationRequest(user);
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

    private void assignRole(TelegramUser user) throws AuthenticationException {
        Objects.requireNonNull(user, "user is null");

        TelegramRole role = userService.retrieveRole(user);

        if (role == null) {
            if (afterRestricted != null) {
                afterRestricted.accept(user);
            }
            throw new AuthenticationException(String.format("Phone %s not matched with any role for user %s",
                    user.getPhone(), user.getUsername()));
        }

        user.setRole(role);
        userService.save(user);

        logger.info("Role {} matched and assigned to user {}", role, user.getUsername());
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
            userService.save(user);
        }

        return user;
    }

    private void startAuthorizationProcess(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        user.setAuthState(AuthState.AUTHORIZATION);
        userService.save(user);
        sendAuthorizationRequest(user);
    }

    private void sendAuthorizationRequest(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton()
                .setRequestContact(true)
                .setText(messageService.getMessage("authentication.authorizeButton")));
        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(user.getUserId())
                    .setText(messageService.getMessage("authentication.authorizeMessage"))
                    .setReplyMarkup(new ReplyKeyboardMarkup()
                            .setResizeKeyboard(true)
                            .setKeyboard(Collections.singletonList(keyboardRow))));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending authorization request to user %s",
                    user.getUsername()), e);
        }
    }

    private void completeAuthorizationProcess(TelegramUser user) {
        Objects.requireNonNull(user, "user is null");

        user.setAuthState(AuthState.AUTHORIZED);
        userService.save(user);
        if (afterAuthorized != null) {
            afterAuthorized.accept(user);
        }
    }

}
