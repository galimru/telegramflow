package org.telegram.telegramflow.api;

import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.xml.ScreenDefinition;

public interface ScreenRegistry {

    void initialize() throws ScreenRegistryException;

    void setDescriptorPath(String descriptorPath);

    ScreenDefinition get(String screenId);

    int size();
}
