package org.telegram.telegramflow.defaults;

import org.telegram.telegramflow.api.AuthenticationService;
import org.telegram.telegramflow.api.TelegramService;
import org.telegram.telegramflow.api.UserService;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.common.AuthState;
import org.telegram.telegramflow.common.Role;
import org.telegram.telegramflow.common.User;
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

import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

public class DefaultAuthenticationService implements AuthenticationService {

    private Logger logger = LoggerFactory.getLogger(DefaultAuthenticationService.class);

    private final static String DEFAULT_AUTHORIZE_MESSAGE = "Please share your phone to authorize";
    private final static String DEFAULT_AUTHORIZE_BUTTON = "Share phone";
    private final static String DEFAULT_AUTHORIZED_MESSAGE = "Authorized";
    private final static String DEFAULT_RESTRICTED_MESSAGE = "Restricted";

    private final static ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private UserService userService;

    private TelegramService telegramService;

    private String authorizeMessage = DEFAULT_AUTHORIZE_MESSAGE;

    private String authorizeButton = DEFAULT_AUTHORIZE_BUTTON;

    private String authorizedMessage = DEFAULT_AUTHORIZED_MESSAGE;

    private String restrictedMessage = DEFAULT_RESTRICTED_MESSAGE;

    private Consumer<User> afterAuthorized = (user) -> {
        try {
            telegramService.executeMethod(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(authorizedMessage));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending authorized message to user %s",
                    user.getUsername()), e);
        }
    };

    private Consumer<User> afterRestricted = (user) -> {
        try {
            telegramService.executeMethod(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(restrictedMessage)
                    .setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending restricted message to user %s",
                    user.getUsername()), e);
        }
    };

    public DefaultAuthenticationService() {
    }

    public DefaultAuthenticationService(UserService userService, TelegramService telegramService) {
        this.userService = userService;
        this.telegramService = telegramService;
    }

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setTelegramService(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    public DefaultAuthenticationService setAuthorizeMessage(String authorizeMessage) {
        this.authorizeMessage = authorizeMessage;
        return this;
    }

    public DefaultAuthenticationService setAuthorizeButton(String authorizeButton) {
        this.authorizeButton = authorizeButton;
        return this;
    }

    public DefaultAuthenticationService setAuthorizedMessage(String authorizedMessage) {
        this.authorizedMessage = authorizedMessage;
        return this;
    }

    public DefaultAuthenticationService setRestrictedMessage(String restrictedMessage) {
        this.restrictedMessage = restrictedMessage;
        return this;
    }

    public DefaultAuthenticationService setAfterAuthorized(Consumer<User> afterAuthorized) {
        this.afterAuthorized = afterAuthorized;
        return this;
    }

    public DefaultAuthenticationService setAfterRestricted(Consumer<User> afterRestricted) {
        this.afterRestricted = afterRestricted;
        return this;
    }

    @Override
    public User authorize(Update update) throws AuthenticationException {
        User user = retrieveUser(TelegramUtil.extractFrom(update));

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

    @Override
    public User getCurrentUser() {
        return CURRENT_USER.get();
    }

    @Override
    public void logout(User user) {
        CURRENT_USER.remove();
        user.setAuthState(null);
        userService.save(user);
        logger.info("User {} logged out", user.getUsername());
        startAuthorizationProcess(user);
    }

    private void assignRole(User user) throws AuthenticationException {
        Role role = userService.retrieveRole(user);

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

    private User retrieveUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Objects.requireNonNull(telegramUser, "telegramUser is null");

        User user = userService.get(String.valueOf(telegramUser.getId()));

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

    private void startAuthorizationProcess(User user) {
        user.setAuthState(AuthState.AUTHORIZATION);
        userService.save(user);
        sendAuthorizationRequest(user);
    }

    private void sendAuthorizationRequest(User user) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton()
                .setRequestContact(true)
                .setText(authorizeButton));
        try {
            telegramService.executeMethod(new SendMessage(String.valueOf(user.getUserId()), authorizeMessage)
                    .setReplyMarkup(new ReplyKeyboardMarkup()
                            .setResizeKeyboard(true)
                            .setKeyboard(Collections.singletonList(keyboardRow))));
        } catch (TelegramApiException e) {
            logger.error(String.format("An error occurred while sending authorization request to user %s",
                    user.getUsername()), e);
        }
    }

    private void completeAuthorizationProcess(User user) {
        user.setAuthState(AuthState.AUTHORIZED);
        userService.save(user);
        if (afterAuthorized != null) {
            afterAuthorized.accept(user);
        }
    }

}
