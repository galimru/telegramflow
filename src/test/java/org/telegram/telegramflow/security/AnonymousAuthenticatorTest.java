package org.telegram.telegramflow.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.objects.AuthState;
import org.telegram.telegramflow.dummy.DummyRole;
import org.telegram.telegramflow.dummy.DummyUser;
import org.telegram.telegramflow.objects.TelegramUser;
import org.telegram.telegramflow.services.MessageProvider;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;
import utils.JsonUtil;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnonymousAuthenticatorTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private MessageProvider messageProvider;

    @Mock
    private UserService userService;

    private Authenticator authenticator;

    @Before
    public void setup() {
        authenticator = new AnonymousAuthenticator();
        authenticator.setMessageProvider(messageProvider);
        authenticator.setTelegramBot(telegramBot);
        authenticator.setUserService(userService);
    }

    @Test
    public void shouldReturnUserForNewUser() throws IOException, AuthenticationException {
        when(userService.find(any())).thenReturn(null);
        when(userService.create()).thenReturn(new DummyUser());

        Update update = JsonUtil.fromFile("/json/update_start.json");

        TelegramUser authorizedUser = authenticator.authorize(update);

        assertNotNull(authorizedUser);
    }

    @Test
    public void shouldReturnUserForAuthorizedUser() throws IOException, AuthenticationException {
        when(userService.find(any())).thenReturn(null);
        when(userService.create()).then(i -> {
            TelegramUser user = new DummyUser();
            user.setAuthState(AuthState.AUTHORIZED);
            user.setRole(DummyRole.USER);
            return user;
        });

        Update update = JsonUtil.fromFile("/json/update_start.json");

        TelegramUser authorizedUser = authenticator.authorize(update);

        assertNotNull(authorizedUser);
    }
}
