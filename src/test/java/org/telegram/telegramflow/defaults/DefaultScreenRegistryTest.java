package org.telegram.telegramflow.defaults;

import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;

public class DefaultScreenRegistryTest {

    @Test
    public void shouldBeInitializedWithEmptyDescriptor() throws ScreenRegistryException {
        DefaultScreenRegistry screenRegistry = new DefaultScreenRegistry();
        screenRegistry.setDescriptorPath("shouldBeInitializedWithEmptyDescriptor/screens.xml");
        screenRegistry.initialize();
        Assert.assertEquals(0, screenRegistry.size());
    }

    @Test
    public void shouldBeInitializedWithoutActions() throws ScreenRegistryException {
        DefaultScreenRegistry screenRegistry = new DefaultScreenRegistry();
        screenRegistry.setDescriptorPath("shouldBeInitializedWithoutActions/screens.xml");
        screenRegistry.initialize();
        Assert.assertNull(screenRegistry.get("default").getButtons());
    }

}
