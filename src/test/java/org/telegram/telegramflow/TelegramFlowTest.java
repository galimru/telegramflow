package org.telegram.telegramflow;

import org.junit.Test;
import org.telegram.telegramflow.api.DummyTelegramBot;
import org.telegram.telegramflow.api.DummyUserService;

public class TelegramFlowTest {

    @Test
    public void shouldBeInitializedByDefault() {
        new TelegramFlow()
                .setUserService(new DummyUserService())
                .setTelegramBot(new DummyTelegramBot())
                .configure()
                .initialize();
    }
}
