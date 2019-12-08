package org.telegram.telegramflow.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.dummy.AuthenticationService;
import org.telegram.telegramflow.dummy.MessageService;
import org.telegram.telegramflow.dummy.TelegramBot;
import org.telegram.telegramflow.dummy.UserService;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.objects.AuthState;
import org.telegram.telegramflow.objects.DummyRole;
import org.telegram.telegramflow.objects.DummyUser;
import org.telegram.telegramflow.objects.TelegramUser;
import utils.JsonUtil;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnonymousAuthenticationServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    private AuthenticationService authenticationService;

    @Before
    public void setup() {
        authenticationService = new AnonymousAuthenticationService();
        authenticationService.setMessageService(messageService);
        authenticationService.setTelegramBot(telegramBot);
        authenticationService.setUserService(userService);
    }

    @Test
    public void shouldReturnUserForNewUser() throws IOException, AuthenticationException {
        when(userService.find(any())).thenReturn(null);
        when(userService.create()).thenReturn(new DummyUser());

        Update update = JsonUtil.fromFile("/updates/update_1.json");

        TelegramUser authorizedUser = authenticationService.authorize(update);

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

        Update update = JsonUtil.fromFile("/updates/update_1.json");

        TelegramUser authorizedUser = authenticationService.authorize(update);

        assertNotNull(authorizedUser);
    }
}
