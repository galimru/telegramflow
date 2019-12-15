package org.telegram.telegramflow.services;

import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegramflow.actions.HelpCallbackAction;
import org.telegram.telegramflow.exceptions.ProcessException;

public class CallbackServiceTest {

    @Test
    public void shouldHaveHelpActionRegistered() throws ProcessException {
        CallbackService callbackService = new CallbackService();
        callbackService.register(HelpCallbackAction.ACTION_ID, new HelpCallbackAction());
        Assert.assertNotNull(callbackService.get(HelpCallbackAction.ACTION_ID));
    }

}
