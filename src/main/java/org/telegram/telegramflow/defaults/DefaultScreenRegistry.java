package org.telegram.telegramflow.defaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegramflow.api.ScreenRegistry;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.xml.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultScreenRegistry implements ScreenRegistry {

    private Logger logger = LoggerFactory.getLogger(DefaultScreenRegistry.class);

    private String descriptorPath;
    private Path relativePath;

    private final Map<String, ScreenDefinition> screens = new ConcurrentHashMap<>();

    @Override
    public void initialize() throws ScreenRegistryException {
        logger.info("Initializing screen registry");
        logger.info("Loading screen descriptor from path {}", descriptorPath);
        relativePath = Paths.get(descriptorPath).getParent();
        ScreensDescriptor descriptor = loadDescriptor(descriptorPath);
        if (descriptor.getScreens() != null) {
            logger.info("Registering {} screen definitions", descriptor.getScreens().size());
            for (ScreenIndex index : descriptor.getScreens()) {
                register(index.getId(), index.getSrc());
                logger.info("Registered '{}' screen definition", index.getId());
            }
            postValidate();
        }
    }

    @Override
    public void setDescriptorPath(String descriptorPath) {
        this.descriptorPath = descriptorPath;
    }

    @Override
    public ScreenDefinition get(String screenId) {
        return screens.get(screenId);
    }

    @Override
    public int size() {
        return screens.size();
    }

    private void register(String id, String src) throws ScreenRegistryException {
        ScreenDefinition screen = loadScreen(src);
        screen.setId(id);
        screens.put(id, screen);
    }

    private ScreensDescriptor loadDescriptor(String file) throws ScreenRegistryException {
        try {
            JAXBContext context = JAXBContext.newInstance(ScreensDescriptor.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = getClass().getClassLoader().getResourceAsStream(file);
            if (is == null) {
                throw new ScreenRegistryException(String.format("Screens descriptor [%s] does not exist", file));
            }
            return (ScreensDescriptor) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new ScreenRegistryException(String.format("Cannot parse screen descriptor [%s]", file), e);
        }
    }

    private ScreenDefinition loadScreen(String file) throws ScreenRegistryException {
        try {
            JAXBContext context = JAXBContext.newInstance(ScreenDefinition.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (relativePath != null) {
                file = relativePath.resolve(file).toString();
            }
            InputStream is = getClass().getClassLoader().getResourceAsStream(file);
            if (is == null) {
                throw new ScreenRegistryException(
                        String.format("Screen definition [%s] does not exist, please check %s",
                                file, descriptorPath));
            }
            return (ScreenDefinition) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new ScreenRegistryException(String.format("Cannot parse screen definition [%s]", file), e);
        }
    }

    private void validateScreen(ScreenDefinition screen) throws ScreenRegistryException {
        if (screen.getButtons() != null) {
           for (Object button : screen.getButtons()) {
               validateButton(button);
           }
        }
    }

    private void validateButton(Object button) throws ScreenRegistryException {
        if (button instanceof ButtonDefinition) {
            ButtonDefinition definition = (ButtonDefinition) button;
            if (definition.getTransitTo() == null && definition.getAction() == null) {
                throw new ScreenRegistryException(String
                        .format("At least one property transitTo or action must be defined [button: %s]",
                                definition.getName()));
            }
            if (definition.getTransitTo() != null
                    && !screens.containsKey(definition.getTransitTo())) {
                throw new ScreenRegistryException(String.format("Screen '%s' defined in transitTo not found [button: %s]",
                        definition.getTransitTo(), definition.getName()));
            }
        } else if (button instanceof ButtonRowDefinition) {
            ButtonRowDefinition definition = (ButtonRowDefinition) button;
            for (ButtonDefinition buttonDefinition : definition.getButtons()) {
                validateButton(buttonDefinition);
            }
        } else {
            throw new ScreenRegistryException(String.format("Cannot infer button class from object %s", button));
        }
    }

    private void postValidate() throws ScreenRegistryException {
        for(ScreenDefinition screen : screens.values()) {
            validateScreen(screen);
        }
    }

}
