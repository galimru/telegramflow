package org.telegram.telegramflow.defaults;

import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegramflow.api.MessageService;

import java.io.IOException;

public class DefaultMessageServiceTest {

    @Test
    public void shouldGetMessageByKey() throws IOException {
        MessageService messageService = new DefaultMessageService();
        messageService.initialize();
        String message = messageService.getMessage("authentication.authorizeMessage");
        Assert.assertNotNull(message);
    }
}
