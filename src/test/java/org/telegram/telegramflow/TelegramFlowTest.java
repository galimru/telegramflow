package org.telegram.telegramflow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.actions.HelpCallbackAction;
import org.telegram.telegramflow.dummy.DummyRole;
import org.telegram.telegramflow.dummy.DummyUser;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.services.CallbackService;
import org.telegram.telegramflow.objects.AuthState;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;
import utils.JsonUtil;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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

    @Test(expected = UnsupportedOperationException.class)
    public void shouldProcessDefaultCallbackHandler() throws IOException, AuthenticationException, ProcessException {
        TelegramFlow telegramFlow = new TelegramFlow()
                .setUserService(userService)
                .setTelegramBot(telegramBot)
                .setDefaultCallbackHandler(new CallbackService()
                        .register(HelpCallbackAction.ACTION_ID, new HelpCallbackAction())
                        .createHandler())
                .configure()
                .initialize();

        when(userService.find(any())).thenReturn(null);
        when(userService.create()).then(i -> {
            TelegramUser user = new DummyUser();
            user.setAuthState(AuthState.AUTHORIZED);
            user.setActiveScreen("default");
            user.setRole(DummyRole.USER);
            return user;
        });

        Update update = JsonUtil.fromFile("/json/update_callback_help.json");
        telegramFlow.process(update);
    }

}
