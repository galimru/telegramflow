package org.telegram.telegramflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.exceptions.InitializationException;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.handlers.KeyboardAction;
import org.telegram.telegramflow.handlers.UpdateHandler;
import org.telegram.telegramflow.services.*;
import org.telegram.telegramflow.security.Authenticator;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.security.AnonymousAuthenticator;
import org.telegram.telegramflow.services.defaults.DefaultInitialScreenProvider;
import org.telegram.telegramflow.services.defaults.DefaultMessageProvider;
import org.telegram.telegramflow.services.defaults.DefaultScreenRegistry;
import org.telegram.telegramflow.utils.TelegramUtil;
import org.telegram.telegramflow.xml.definition.ButtonDefinition;
import org.telegram.telegramflow.xml.definition.ButtonRowDefinition;
import org.telegram.telegramflow.xml.definition.MessageDefinition;
import org.telegram.telegramflow.xml.definition.ScreenDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramFlow {

    private final static String START_COMMAND = "/start";

    private Logger logger = LoggerFactory.getLogger(TelegramFlow.class);

    private Authenticator authenticator;
    private ScreenRegistry screenRegistry;
    private InitialScreenProvider initialScreenProvider;
    private UserService userService;
    private TelegramBot telegramBot;
    private MessageProvider messageProvider;

    private boolean initialized;

    private Class<? extends UpdateHandler> defaultInputHandler = null;

    private Class<? extends UpdateHandler> defaultCallbackHandler = null;

    private final Map<Class<? extends UpdateHandler>, UpdateHandler> cachedHandlers = new ConcurrentHashMap<>();

    private final Map<Class<? extends KeyboardAction>, KeyboardAction> cachedActions = new ConcurrentHashMap<>();

    @Nonnull
    public TelegramFlow configure() {
        if (screenRegistry == null) {
            screenRegistry = new DefaultScreenRegistry();
        }
        if (authenticator == null) {
            authenticator = new AnonymousAuthenticator();
        }
        if (initialScreenProvider == null) {
            initialScreenProvider = new DefaultInitialScreenProvider();
        }
        if (messageProvider == null) {
            messageProvider = new DefaultMessageProvider();
        }
        return this;
    }

    @Nonnull
    public TelegramFlow initialize() {
        Objects.requireNonNull(screenRegistry, "screenRegistry is null");
        Objects.requireNonNull(authenticator, "authenticator is null");
        Objects.requireNonNull(initialScreenProvider, "initialScreenProvider is null");
        Objects.requireNonNull(userService, "userService is null");
        Objects.requireNonNull(telegramBot, "telegramBot is null");
        Objects.requireNonNull(messageProvider, "messageProvider is null");

        try {
            screenRegistry.initialize();
        } catch (ScreenRegistryException e) {
            throw new InitializationException("Cannot initialize screen registry", e);
        }

        authenticator.setUserService(userService);
        authenticator.setTelegramBot(telegramBot);
        authenticator.setMessageProvider(messageProvider);

        initialized = true;

        return this;
    }

    public ScreenRegistry getScreenRegistry() {
        return screenRegistry;
    }

    @Nonnull
    public TelegramFlow setScreenRegistry(@Nonnull ScreenRegistry screenRegistry) {
        this.screenRegistry = screenRegistry;
        return this;
    }

    public UserService getUserService() {
        return userService;
    }

    @Nonnull
    public TelegramFlow setUserService(@Nonnull UserService userService) {
        this.userService = userService;
        return this;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }

    @Nonnull
    public TelegramFlow setTelegramBot(@Nonnull TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        return this;
    }

    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    @Nonnull
    public TelegramFlow setMessageProvider(@Nonnull MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
        return this;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @Nonnull
    public TelegramFlow setAuthenticator(@Nonnull Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    public InitialScreenProvider getInitialScreenProvider() {
        return initialScreenProvider;
    }

    @Nonnull
    public TelegramFlow setInitialScreenProvider(@Nonnull InitialScreenProvider initialScreenProvider) {
        this.initialScreenProvider = initialScreenProvider;
        return this;
    }

    public Class<? extends UpdateHandler> getDefaultInputHandler() {
        return defaultInputHandler;
    }

    @Nonnull
    public TelegramFlow setDefaultInputHandler(@Nullable Class<? extends UpdateHandler> defaultInputHandler) {
        this.defaultInputHandler = defaultInputHandler;
        return this;
    }

    public Class<? extends UpdateHandler> getDefaultCallbackHandler() {
        return defaultCallbackHandler;
    }

    @Nonnull
    public TelegramFlow setDefaultCallbackHandler(@Nullable Class<? extends UpdateHandler> defaultCallbackHandler) {
        this.defaultCallbackHandler = defaultCallbackHandler;
        return this;
    }

    public void process(@Nonnull Update update) throws AuthenticationException, ProcessException {
        if (!initialized) {
            throw new ProcessException("Telegram flow is not initialized");
        }

        Objects.requireNonNull(update, "update is null");

        TelegramUser user = authenticator.authorize(update);
        try {
            String text = TelegramUtil.extractText(update);
            if (START_COMMAND.equals(text)) {
                user.setActiveScreen(null);
            }
            if (user.getActiveScreen() != null) {
                ScreenDefinition screen;
                try {
                    screen = screenRegistry.get(user.getActiveScreen());
                } catch (ScreenRegistryException e) {
                    throw new ProcessException(e);
                }
                process(update, screen);
            } else {
                String initialScreen = initialScreenProvider.getInitialScreen(user);
                transitTo(initialScreen);
            }
        } finally {
            authenticator.end();
        }
    }

    private void process(Update update, ScreenDefinition screen) throws ProcessException {
        Objects.requireNonNull(update, "update is null");
        Objects.requireNonNull(screen, "screen is null");

        Map<String, ButtonDefinition> buttons = getButtons(screen);
        if (update.hasMessage() && update.getMessage().hasText()
                && buttons.containsKey(update.getMessage().getText())) {
            String text = update.getMessage().getText();
            ButtonDefinition button = buttons.get(text);
            executeAction(update, button);
        } else if (update.hasCallbackQuery() && screen.getCallback() != null) {
            invokeHandler(update, screen.getCallback().getHandlerClass());
        } else if (update.hasCallbackQuery() && defaultCallbackHandler != null) {
            invokeHandler(update, defaultCallbackHandler);
        } else if (screen.getInput() != null) {
            invokeHandler(update, screen.getInput().getHandlerClass());
        } else if (defaultInputHandler != null) {
            invokeHandler(update, defaultInputHandler);
        }
    }

    public void transitTo(@Nonnull String screenId) throws ProcessException {
        transitTo(screenId, null);
    }

    public void transitTo(@Nonnull String screenId, @Nullable String message) throws ProcessException {
        Objects.requireNonNull(screenId, "screenId is null");

        TelegramUser user = authenticator.getUser();

        logger.info("Transiting user {} to screen {}", user.getUsername(), screenId);

        ScreenDefinition screen;
        try {
            screen = screenRegistry.get(screenId);
        } catch (ScreenRegistryException e) {
            throw new ProcessException(e);
        }

        show(message, screen);

        user.setActiveScreen(screen.getId());
        userService.save(user);
    }

    private void invokeHandler(Update update, Class<? extends UpdateHandler> handlerClass) throws ProcessException {
        Objects.requireNonNull(update, "update is null");
        Objects.requireNonNull(handlerClass, "handlerClass is null");

        TelegramUser user = authenticator.getUser();

        logger.info("Invoking handler {} by user {}", handlerClass.getSimpleName(), user.getUsername());

        UpdateHandler handler;
        if (cachedHandlers.containsKey(handlerClass)) {
            handler = cachedHandlers.get(handlerClass);
        } else {
            try {
                Constructor constructor = handlerClass.getConstructor();
                handler = (UpdateHandler) constructor.newInstance();
                cachedHandlers.put(handlerClass, handler);
            } catch (InstantiationException | InvocationTargetException
                    | IllegalAccessException | NoSuchMethodException e) {
                throw new ProcessException("Cannot invoke handler " + handlerClass.getName(), e);
            }
        }
        handler.handle(update);
    }

    private void executeAction(Update update, ButtonDefinition button) throws ProcessException {
        Objects.requireNonNull(update, "update is null");
        Objects.requireNonNull(button, "button is null");

        TelegramUser user = authenticator.getUser();

        logger.info("Executing action {} by user {}", button.getName(), user.getUsername());

        if (button.getAction() != null) {
            invokeAction(update, button.getAction());
        } else if (button.getTransitTo() != null) {
            transitTo(button.getTransitTo());
        }
    }

    private void invokeAction(Update update, Class<? extends KeyboardAction> actionClass) throws ProcessException {
        Objects.requireNonNull(update, "update is null");
        Objects.requireNonNull(actionClass, "actionClass is null");

        TelegramUser user = authenticator.getUser();

        logger.info("Invoking action {} by user {}", actionClass.getSimpleName(), user.getUsername());

        KeyboardAction action;
        if (cachedActions.containsKey(actionClass)) {
            action = cachedActions.get(actionClass);
        } else {
            try {
                Constructor constructor = actionClass.getConstructor();
                action = (KeyboardAction) constructor.newInstance();
                cachedActions.put(actionClass, action);
            } catch (InstantiationException | InvocationTargetException
                    | IllegalAccessException | NoSuchMethodException e) {
                throw new ProcessException("Cannot invoke action " + actionClass.getName(), e);
            }
        }
        action.execute(update);
    }

    private Map<String, ButtonDefinition> getButtons(ScreenDefinition screen) {
        Objects.requireNonNull(screen, "screen is null");

        Map<String, ButtonDefinition> buttons = new HashMap<>();
        if (screen.getButtons() != null) {
            for (Object obj : screen.getButtons()) {
                if (obj instanceof ButtonRowDefinition) {
                    ButtonRowDefinition buttonRow = (ButtonRowDefinition) obj;
                    buttonRow.getButtons().forEach(button ->
                            buttons.put(button.getName(), button));
                }
                if (obj instanceof ButtonDefinition) {
                    ButtonDefinition button = (ButtonDefinition) obj;
                    buttons.put(button.getName(), button);
                }
            }
        }
        return buttons;
    }


    private void show(@Nullable String text, ScreenDefinition screen) throws ProcessException {
        MessageDefinition message = screen.getMessage();

        if (text == null) {
            text = message.getText();
        }

        ReplyKeyboard replyKeyboard;
        List<KeyboardRow> keyboardRows = createKeyboardRows(screen);
        if (keyboardRows.isEmpty()) {
            replyKeyboard = new ReplyKeyboardRemove();
        } else {
            replyKeyboard = new ReplyKeyboardMarkup()
                    .setResizeKeyboard(true)
                    .setKeyboard(keyboardRows);
        }

        TelegramUser user = authenticator.getUser();

        String parseMode = null;
        if (message.getFormat() != null) {
            parseMode = message.getFormat().name();
        }

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(text)
                    .setParseMode(parseMode)
                    .setReplyMarkup(replyKeyboard));
        } catch (TelegramApiException e) {
            throw new ProcessException("An error occurred while sending telegram message", e);
        }
    }

    @Nonnull
    private List<KeyboardRow> createKeyboardRows(@Nonnull ScreenDefinition screen) {
        Objects.requireNonNull(screen, "screen is null");

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        if (screen.getButtons() != null) {
            for (Object obj : screen.getButtons()) {
                if (obj instanceof ButtonDefinition) {
                    ButtonDefinition button = (ButtonDefinition) obj;
                    KeyboardRow keyboardRow = new KeyboardRow();
                    keyboardRow.add(new KeyboardButton().setText(button.getName()));
                    keyboardRows.add(keyboardRow);
                }
                if (obj instanceof ButtonRowDefinition) {
                    ButtonRowDefinition buttonRow = (ButtonRowDefinition) obj;
                    KeyboardRow keyboardRow = new KeyboardRow();
                    buttonRow.getButtons().forEach(button ->
                            keyboardRow.add(new KeyboardButton()
                                    .setText(button.getName())));
                    keyboardRows.add(keyboardRow);
                }
            }
        }
        return keyboardRows;
    }

}
