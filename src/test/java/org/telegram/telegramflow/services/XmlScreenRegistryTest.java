package org.telegram.telegramflow.services;

import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegramflow.api.ScreenRegistry;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;

public class XmlScreenRegistryTest {

    @Test
    public void shouldBeInitializedWithEmptyDescriptor() throws ScreenRegistryException {
        ScreenRegistry screenRegistry = new XmlScreenRegistry();
        screenRegistry.setDescriptorPath("shouldBeInitializedWithEmptyDescriptor/screens.xml");
        screenRegistry.initialize();
        Assert.assertEquals(0, screenRegistry.size());
    }

    @Test
    public void shouldBeInitializedWithoutActions() throws ScreenRegistryException {
        ScreenRegistry screenRegistry = new XmlScreenRegistry();
        screenRegistry.setDescriptorPath("shouldBeInitializedWithoutActions/screens.xml");
        screenRegistry.initialize();
        Assert.assertNull(screenRegistry.get("default").getButtons());
    }

}
