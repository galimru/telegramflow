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
import org.telegram.telegramflow.api.*;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.exceptions.InitializationException;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.handlers.KeyboardAction;
import org.telegram.telegramflow.handlers.UpdateHandler;
import org.telegram.telegramflow.objects.AbstractController;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.services.*;
import org.telegram.telegramflow.xml.ButtonDefinition;
import org.telegram.telegramflow.xml.ButtonRowDefinition;
import org.telegram.telegramflow.xml.ScreenDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramFlow {

    private final static String SCREENS_DESCRIPTOR_PATH = "screens.xml";
    private final static String START_COMMAND = "/start";

    private Logger logger = LoggerFactory.getLogger(TelegramFlow.class);

    private AuthenticationService authenticationService;
    private ScreenRegistry screenRegistry;
    private InitialScreenProvider initialScreenProvider;
    private UserService userService;
    private TelegramBot telegramBot;
    private MessageService messageService;

    private boolean initialized;

    private Class<? extends UpdateHandler> defaultInlineHandler = null;

    private Class<? extends UpdateHandler> defaultInputHandler = null;

    private final Map<Class<? extends UpdateHandler>, UpdateHandler> cachedHandlers = new ConcurrentHashMap<>();

    private final Map<Class<? extends KeyboardAction>, KeyboardAction> cachedActions = new ConcurrentHashMap<>();

    @Nonnull
    public TelegramFlow configure() {
        if (screenRegistry == null) {
            screenRegistry = new XmlScreenRegistry();
            screenRegistry.setDescriptorPath(SCREENS_DESCRIPTOR_PATH);
        }
        if (authenticationService == null) {
            authenticationService = new AnonymousAuthenticationService();
        }
        if (initialScreenProvider == null) {
            initialScreenProvider = new DefaultInitialScreenProvider();
        }
        if (messageService == null) {
            messageService = new PropertyMessageService();
        }
        return this;
    }

    @Nonnull
    public TelegramFlow initialize() {
        Objects.requireNonNull(screenRegistry, "screenRegistry is null");
        Objects.requireNonNull(authenticationService, "authenticationService is null");
        Objects.requireNonNull(initialScreenProvider, "initialScreenProvider is null");
        Objects.requireNonNull(userService, "userService is null");
        Objects.requireNonNull(telegramBot, "telegramBot is null");
        Objects.requireNonNull(messageService, "messageService is null");

        try {
            screenRegistry.initialize();
        } catch (ScreenRegistryException e) {
            throw new InitializationException("Cannot initialize screen registry", e);
        }

        try {
            messageService.initialize();
        } catch (IOException e) {
            throw new InitializationException("Cannot initialize message service", e);
        }

        authenticationService.setUserService(userService);
        authenticationService.setTelegramBot(telegramBot);
        authenticationService.setMessageService(messageService);

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

    public MessageService getMessageService() {
        return messageService;
    }

    @Nonnull
    public TelegramFlow setMessageService(@Nonnull MessageService messageService) {
        this.messageService = messageService;
        return this;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    @Nonnull
    public TelegramFlow setAuthenticationService(@Nonnull AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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

    public Class<? extends UpdateHandler> getDefaultInlineHandler() {
        return defaultInlineHandler;
    }

    @Nonnull
    public TelegramFlow setDefaultInlineHandler(@Nullable Class<? extends UpdateHandler> defaultInlineHandler) {
        this.defaultInlineHandler = defaultInlineHandler;
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

    public void process(@Nonnull Update update) throws AuthenticationException, ProcessException {
        if (!initialized) {
            throw new ProcessException("Telegram flow is not initialized");
        }

        Objects.requireNonNull(update, "update is null");

        TelegramUser user = authenticationService.authorize(update);
        try {
            if (update.hasMessage() && update.getMessage().hasText()
                    && update.getMessage().getText().equals(START_COMMAND)) {
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
            authenticationService.end();
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

    public void transitTo(@Nonnull String screenId) throws ProcessException {
        transitTo(screenId, null);
    }

    public void transitTo(@Nonnull String screenId, @Nullable String message) throws ProcessException {
        Objects.requireNonNull(screenId, "screenId is null");

        TelegramUser user = authenticationService.getCurrentUser();

        logger.info("Transiting user {} to screen {}", user.getUsername(), screenId);

        ScreenDefinition screen;
        try {
            screen = screenRegistry.get(screenId);
        } catch (ScreenRegistryException e) {
            throw new ProcessException(e);
        }

        Optional<AbstractController> controller = getController(screen);

        controller.ifPresent(ctrl -> ctrl.init(screen));

        if (message == null) {
            message = screen.getMessage();
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

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(message)
                    .setReplyMarkup(replyKeyboard));
            user.setActiveScreen(screen.getId());
            userService.save(user);
        } catch (TelegramApiException e) {
            throw new ProcessException("An error occurred while sending telegram message", e);
        }

        controller.ifPresent(AbstractController::ready);
    }

    private void invokeHandler(Update update, Class<? extends UpdateHandler> handlerClass) throws ProcessException {
        Objects.requireNonNull(update, "update is null");
        Objects.requireNonNull(handlerClass, "handlerClass is null");

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

    private Optional<AbstractController> getController(ScreenDefinition screen) throws ProcessException {
        Objects.requireNonNull(screen, "screen is null");

        Class<? extends AbstractController> controllerClass = screen.getControllerClass();
        if (controllerClass != null) {
            try {
                Constructor constructor = controllerClass.getConstructor();
                return  Optional.of((AbstractController)constructor.newInstance());
            } catch (InstantiationException | InvocationTargetException
                    | IllegalAccessException | NoSuchMethodException e) {
                throw new ProcessException("Cannot create controller " + controllerClass.getName(), e);
            }
        }
        return Optional.empty();
    }

    private void executeAction(Update update, ButtonDefinition button) throws ProcessException {
        Objects.requireNonNull(update, "update is null");
        Objects.requireNonNull(button, "button is null");

        TelegramUser user = authenticationService.getCurrentUser();

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

    private List<KeyboardRow> createKeyboardRows(ScreenDefinition screen) {
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
