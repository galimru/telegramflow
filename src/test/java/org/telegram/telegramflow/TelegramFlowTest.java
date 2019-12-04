package org.telegram.telegramflow;

import org.junit.Test;
import org.telegram.telegramflow.api.DummyTelegramService;
import org.telegram.telegramflow.api.DummyUserService;

public class TelegramFlowTest {

    @Test
    public void shouldBeInitializedByDefault() {
        new TelegramFlow()
                .setUserService(new DummyUserService())
                .setTelegramService(new DummyTelegramService())
                .configure()
                .initialize();
    }
}
