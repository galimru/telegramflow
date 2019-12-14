package org.telegram.telegramflow.services;

import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegramflow.exceptions.CallbackException;
import org.telegram.telegramflow.services.defaults.CallbackService;

public class CallbackServiceTest {

    @Test
    public void shouldHaveHelpActionRegistered() throws CallbackException {
        CallbackService callbackService = new CallbackService();
        callbackService.initialize();
        Assert.assertNotNull(callbackService.getAction("HELP"));
    }

}
