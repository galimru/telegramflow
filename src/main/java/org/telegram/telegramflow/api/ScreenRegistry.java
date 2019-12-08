package org.telegram.telegramflow.api;

import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.xml.ScreenDefinition;

import javax.annotation.Nonnull;

public interface ScreenRegistry {

    void initialize() throws ScreenRegistryException;

    void setDescriptorPath(String descriptorPath);

    @Nonnull
    ScreenDefinition get(@Nonnull String screenId) throws ScreenRegistryException;

    int size();
}
