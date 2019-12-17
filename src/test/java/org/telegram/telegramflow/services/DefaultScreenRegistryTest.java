package org.telegram.telegramflow.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.xml.screendefinition.ButtonDefinition;
import org.telegram.telegramflow.xml.screendefinition.ScreenDefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DefaultScreenRegistryTest {

    @Test
    public void shouldContainDefaultScreen() throws ScreenRegistryException {
        ScreenRegistry screenRegistry = new DefaultScreenRegistry();
        screenRegistry.initialize();
        ScreenDefinition defaultScreen = screenRegistry.get("default");

        assertNotNull(defaultScreen);
    }

    @Test
    public void shouldBeLoadedWithAllScreenElements() throws ScreenRegistryException {
        ScreenRegistry screenRegistry = new DefaultScreenRegistry();
        screenRegistry.initialize();
        ScreenDefinition definition = screenRegistry.get("fullspec");

        assertNotNull(definition.getMessage());
        assertNotNull(definition.getMessage().getText());
        assertNotNull(definition.getMessage().getFormat());
        assertNotNull(definition.getCallback());
        assertNotNull(definition.getCallback().getHandlerClass());
        assertNotNull(definition.getInput());
        assertNotNull(definition.getInput().getHandlerClass());
        assertNotNull(definition.getButtons());
        assertEquals(2, definition.getButtons().size());
        assertNotNull(((ButtonDefinition)definition.getButtons().get(0)).getName());
        assertNotNull(((ButtonDefinition)definition.getButtons().get(0)).getTransitTo());
        assertNotNull(((ButtonDefinition)definition.getButtons().get(1)).getHandlerClass());
    }

}
