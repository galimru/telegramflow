package org.telegram.telegramflow.services;

import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegramflow.actions.HelpCallbackAction;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.handlers.CallbackService;

public class CallbackServiceTest {

    @Test
    public void shouldHaveHelpActionRegistered() throws ProcessException {
        CallbackService callbackService = new CallbackService();
        callbackService.register(HelpCallbackAction.KEY, new HelpCallbackAction());
        Assert.assertNotNull(callbackService.get(HelpCallbackAction.KEY));
    }

}
