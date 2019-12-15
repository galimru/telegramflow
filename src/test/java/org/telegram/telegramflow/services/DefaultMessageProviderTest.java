package org.telegram.telegramflow.services;

import org.junit.Assert;
import org.junit.Test;

public class DefaultMessageProviderTest {

    private final static String NON_EXISTS_KEY = "non-exists-key";

    @Test
    public void shouldGetReturnKeyIfNotExists() {
        MessageProvider messages = new DefaultMessageProvider();
        String message = messages.getMessage(NON_EXISTS_KEY);
        Assert.assertEquals(NON_EXISTS_KEY, message);
    }
}
