package org.telegram.telegramflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.api.AuthenticationService;
import org.telegram.telegramflow.api.MessageService;
import org.telegram.telegramflow.api.TelegramBot;
import org.telegram.telegramflow.api.UserService;
import org.telegram.telegramflow.common.AuthState;
import org.telegram.telegramflow.common.DummyRole;
import org.telegram.telegramflow.common.DummyUser;
import org.telegram.telegramflow.common.TelegramUser;
import org.telegram.telegramflow.defaults.DefaultAuthenticationService;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import utils.JsonUtil;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAuthenticationServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    private AuthenticationService authenticationService;

    @Before
    public void setup() {
        authenticationService = new DefaultAuthenticationService();
        authenticationService.setMessageService(messageService);
        authenticationService.setTelegramBot(telegramBot);
        authenticationService.setUserService(userService);
    }

    @Test
    public void shouldSendAuthorizeMessageForNewUser() throws IOException, TelegramApiException {
        when(userService.find(any())).thenReturn(null);
        when(userService.create()).thenReturn(new DummyUser());

        Update update = JsonUtil.fromFile("/updates/update_1.json");
        try {
            authenticationService.authorize(update);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertNotNull(e);
        }

        verify(telegramBot).execute(any());
    }

    @Test
    public void shouldNotThrowExceptionForAuthorizedUser() throws IOException, TelegramApiException, AuthenticationException {
        when(userService.find(any())).thenReturn(null);
        when(userService.create()).then(i -> {
            TelegramUser user = new DummyUser();
            user.setAuthState(AuthState.AUTHORIZED);
            user.setRole(DummyRole.USER);
            return user;
        });

        Update update = JsonUtil.fromFile("/updates/update_1.json");

        TelegramUser authorizedUser = authenticationService.authorize(update);

        assertNotNull(authorizedUser);
    }
}
