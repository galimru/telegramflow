package org.telegram.telegramflow.objects;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.api.AuthenticationService;
import org.telegram.telegramflow.api.TelegramBot;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.xml.ButtonDefinition;
import org.telegram.telegramflow.xml.ButtonRowDefinition;
import org.telegram.telegramflow.xml.ScreenDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AbstractController {

    protected AuthenticationService authenticationService;
    protected TelegramBot telegramBot;
    protected ScreenDefinition screen;

    public void setup(@Nonnull AuthenticationService authenticationService,
                      @Nonnull TelegramBot telegramBot,
                      @Nonnull ScreenDefinition screen) {
        this.authenticationService = authenticationService;
        this.telegramBot = telegramBot;
        this.screen = screen;
    }

    public void init() {
    }

    public void ready() {
    }

    public void show() throws ProcessException {
        show(null);
    }

    public void show(@Nullable String message) throws ProcessException {
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

        TelegramUser user = authenticationService.getUser();

        try {
            telegramBot.execute(new SendMessage()
                    .setChatId(String.valueOf(user.getUserId()))
                    .setText(message)
                    .setReplyMarkup(replyKeyboard));
        } catch (TelegramApiException e) {
            throw new ProcessException("An error occurred while sending telegram message", e);
        }
    }

    @Nonnull
    protected List<KeyboardRow> createKeyboardRows(@Nonnull ScreenDefinition screen) {
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
