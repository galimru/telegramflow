package org.telegram.telegramflow;

import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.exceptions.InitializationException;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.common.TelegramUser;
import org.telegram.telegramflow.defaults.DefaultScreenRegistry;
import org.telegram.telegramflow.handlers.KeyboardAction;
import org.telegram.telegramflow.api.ScreenRegistry;
import org.telegram.telegramflow.handlers.UpdateHandler;
import org.telegram.telegramflow.api.AuthenticationService;
import org.telegram.telegramflow.defaults.DefaultAuthenticationService;
import org.telegram.telegramflow.api.TelegramBot;
import org.telegram.telegramflow.api.UserManager;
import org.telegram.telegramflow.xml.ButtonDefinition;
import org.telegram.telegramflow.xml.ButtonRowDefinition;
import org.telegram.telegramflow.xml.ScreenDefinition;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramFlow {

    private final static String DEFAULT_SCREEN_DESCRIPTOR_PATH = "screens.xml";
    private final static String DEFAULT_SCREEN_ID = "default";
    private final static String START_COMMAND = "/start";

    private Logger logger = LoggerFactory.getLogger(TelegramFlow.class);

    private ScreenRegistry screenRegistry;
    private UserManager userManager;
    private TelegramBot telegramBot;
    private AuthenticationService authenticationService;

    private boolean initialized;

    private Class<? extends UpdateHandler> defaultInlineHandler = null;

    private Class<? extends UpdateHandler> defaultInputHandler = null;

    private final Map<Class<? extends UpdateHandler>, UpdateHandler> cachedHandlers = new ConcurrentHashMap<>();

    private final Map<Class<? extends KeyboardAction>, KeyboardAction> cachedActions = new ConcurrentHashMap<>();

    public TelegramFlow configure() {
        if (screenRegistry == null) {
            screenRegistry = new DefaultScreenRegistry();
            screenRegistry.setDescriptorPath(DEFAULT_SCREEN_DESCRIPTOR_PATH);
        }
        if (authenticationService == null) {
            authenticationService = new DefaultAuthenticationService();
        }
        return this;
    }

    public TelegramFlow initialize() {
        Objects.requireNonNull(screenRegistry, "screenRegistry is null");
        Objects.requireNonNull(userManager, "userManager is null");
        Objects.requireNonNull(telegramBot, "telegramBot is null");
        Objects.requireNonNull(authenticationService, "authenticationService is null");

        try {
            screenRegistry.initialize();
        } catch (ScreenRegistryException e) {
            throw new InitializationException("Cannot initialize screen registry", e);
        }

        authenticationService.setUserManager(userManager);
        authenticationService.setTelegramBot(telegramBot);

        initialized = true;

        return this;
    }

    public ScreenRegistry getScreenRegistry() {
        return screenRegistry;
    }

    public TelegramFlow setScreenRegistry(ScreenRegistry screenRegistry) {
        this.screenRegistry = screenRegistry;
        return this;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public TelegramFlow setUserManager(UserManager userManager) {
        this.userManager = userManager;
        return this;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }

    public TelegramFlow setTelegramBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        return this;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public TelegramFlow setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        return this;
    }

    public Class<? extends UpdateHandler> getDefaultInlineHandler() {
        return defaultInlineHandler;
    }

    public TelegramFlow setDefaultInlineHandler(Class<? extends UpdateHandler> defaultInlineHandler) {
        this.defaultInlineHandler = defaultInlineHandler;
        return this;
    }

    public Class<? extends UpdateHandler> getDefaultInputHandler() {
        return defaultInputHandler;
    }

    public TelegramFlow setDefaultInputHandler(Class<? extends UpdateHandler> defaultInputHandler) {
        this.defaultInputHandler = defaultInputHandler;
        return this;
    }

    public void process(Update update) throws AuthenticationException, ProcessException {
        process(update, DEFAULT_SCREEN_ID);
    }

    public void process(Update update, String defaultScreen) throws AuthenticationException, ProcessException {
        if (!initialized) {
            throw new ProcessException("Telegram flow is not initialized");
        }

        Objects.requireNonNull(defaultScreen, "defaultScreen is null");

        TelegramUser user = authenticationService.authorize(update);

        if (update.hasMessage() && update.getMessage().hasText()
                && update.getMessage().getText().equals(START_COMMAND)) {
            user.setActiveScreen(null);
        }

        if (user.getActiveScreen() != null) {
            ScreenDefinition screen = screenRegistry.get(user.getActiveScreen());
            process(update, screen);
        } else {
            transitTo(defaultScreen);
        }
    }

    private void process(Update update, ScreenDefinition screen) throws ProcessException {
        Map<String, ButtonDefinition> buttons = getButtons(screen);
        if (update.hasMessage() && update.getMessage().hasText()
                && buttons.containsKey(update.getMessage().getText())) {
            String text = update.getMessage().getText();
            ButtonDefinition button = buttons.get(text);
            executeAction(update, button);
        } else if (update.hasCallbackQuery() && screen.getInlineHandler() != null) {
            invokeHandler(update, screen.getInlineHandler().getHandlerClass());
        } else if (update.hasCallbackQuery() && defaultInlineHandler != null) {
            invokeHandler(update, defaultInlineHandler);
        } else if (screen.getInputHandler() != null) {
            invokeHandler(update, screen.getInputHandler().getHandlerClass());
        } else if (defaultInputHandler != null) {
            invokeHandler(update, defaultInputHandler);
        }
    }

    public void transitTo(String screenId) throws ProcessException {
        transitTo(screenId, null);
    }

    public void transitTo(String screenId, String message) throws ProcessException {
        TelegramUser user = authenticationService.getCurrentUser();

        logger.info("Transiting user {} to screen {}", user.getUsername(), screenId);

        ScreenDefinition screen = screenRegistry.get(screenId);
        if (screen == null) {
            throw new ProcessException(String.format("Screen %s is not registered", screenId));
        }

        if (message == null) {
            message = screen.getMessage();
        }

        List<KeyboardRow> keyboardRows = createKeyboardRows(screen);
        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(message)
                    .setReplyMarkup(new ReplyKeyboardMarkup()
                            .setResizeKeyboard(true)
                            .setKeyboard(keyboardRows)));
            user.setActiveScreen(screen.getId());
            userManager.save(user);
        } catch (TelegramApiException e) {
            throw new ProcessException("An error occurred while sending telegram message", e);
        }
    }

    private void invokeHandler(Update update, Class<? extends UpdateHandler> handlerClass) throws ProcessException {
        Preconditions.checkNotNull(handlerClass, "handlerClass is null");
        TelegramUser user = authenticationService.getCurrentUser();

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
        TelegramUser user = authenticationService.getCurrentUser();

        logger.info("Executing action {} by user {}", button.getName(), user.getUsername());

        if (button.getAction() != null) {
            invokeAction(update, button.getAction());
        } else if (button.getTransitTo() != null) {
            transitTo(button.getTransitTo());
        }
    }

    private void invokeAction(Update update, Class<? extends KeyboardAction> actionClass) throws ProcessException {
        Objects.requireNonNull(actionClass, "actionClass is null");
        TelegramUser user = authenticationService.getCurrentUser();

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
        Map<String, ButtonDefinition> buttons = new HashMap<>();
        for(Object obj : screen.getButtons()) {
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
        return buttons;
    }

    private List<KeyboardRow> createKeyboardRows(ScreenDefinition screen) {
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
