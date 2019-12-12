package org.telegram.telegramflow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;

@RunWith(MockitoJUnitRunner.class)
public class TelegramFlowTest {

    @Mock
    private UserService userService;

    @Mock
    private TelegramBot telegramBot;

    @Test
    public void shouldBeInitializedByDefault() {
        new TelegramFlow()
                .setUserService(userService)
                .setTelegramBot(telegramBot)
                .configure()
                .initialize();
    }

}
