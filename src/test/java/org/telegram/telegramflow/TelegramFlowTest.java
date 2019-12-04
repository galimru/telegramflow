package org.telegram.telegramflow;

import org.junit.Test;
import org.telegram.telegramflow.api.DummyTelegramBot;
import org.telegram.telegramflow.api.DummyUserManager;

public class TelegramFlowTest {

    @Test
    public void shouldBeInitializedByDefault() {
        new TelegramFlow()
                .setUserManager(new DummyUserManager())
                .setTelegramBot(new DummyTelegramBot())
                .configure()
                .initialize();
    }
}
