package org.telegram.telegramflow.services;

import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegramflow.dummy.MessageService;

import java.io.IOException;

public class PropertyMessageServiceTest {

    @Test
    public void shouldGetMessageByKey() throws IOException {
        MessageService messageService = new PropertyMessageService();
        messageService.initialize();
        String message = messageService.getMessage("authentication.authorizeMessage");
        Assert.assertNotNull(message);
    }
}
