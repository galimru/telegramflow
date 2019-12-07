package org.telegram.telegramflow.defaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegramflow.api.ScreenRegistry;
import org.telegram.telegramflow.exceptions.ProcessException;
import org.telegram.telegramflow.exceptions.ScreenRegistryException;
import org.telegram.telegramflow.xml.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
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

    @Nullable
    @Override
    public ScreenDefinition get(@Nonnull String screenId) throws ScreenRegistryException {
        Objects.requireNonNull(screens, "screenId is null");

        ScreenDefinition definition = screens.get(screenId);
        if (definition == null) {
            throw new ScreenRegistryException(String.format("Screen '%s' not found in registry", screenId));
        }
        return definition;
    }

    @Override
    public int size() {
        return screens.size();
    }

    private void register(String id, String src) throws ScreenRegistryException {
        Objects.requireNonNull(screens, "id is null");
        Objects.requireNonNull(screens, "src is null");

        ScreenDefinition screen = loadScreen(src);
        screen.setId(id);
        screens.put(id, screen);
    }

    private ScreensDescriptor loadDescriptor(String file) throws ScreenRegistryException {
        Objects.requireNonNull(screens, "file is null");

        try {
            JAXBContext context = JAXBContext.newInstance(ScreensDescriptor.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = getClass().getClassLoader().getResourceAsStream(file);
            if (is == null) {
                throw new ScreenRegistryException(String.format("Screens descriptor '%s' does not exist", file));
            }
            return (ScreensDescriptor) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new ScreenRegistryException(String.format("Cannot parse screen descriptor [%s]", file), e);
        }
    }

    private ScreenDefinition loadScreen(String file) throws ScreenRegistryException {
        Objects.requireNonNull(screens, "file is null");

        try {
            JAXBContext context = JAXBContext.newInstance(ScreenDefinition.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (relativePath != null) {
                file = relativePath.resolve(file).toString();
            }
            InputStream is = getClass().getClassLoader().getResourceAsStream(file);
            if (is == null) {
                throw new ScreenRegistryException(
                        String.format("Screen definition '%s' does not exist, please check %s",
                                file, descriptorPath));
            }
            return (ScreenDefinition) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new ScreenRegistryException(String.format("Cannot parse screen definition [%s]", file), e);
        }
    }

    private void validateScreen(ScreenDefinition screen) throws ScreenRegistryException {
        Objects.requireNonNull(screens, "screen is null");

        if (screen.getButtons() != null) {
           for (Object button : screen.getButtons()) {
               validateButton(button);
           }
        }
    }

    private void validateButton(Object button) throws ScreenRegistryException {
        Objects.requireNonNull(screens, "button is null");

        if (button instanceof ButtonDefinition) {
            ButtonDefinition definition = (ButtonDefinition) button;
            if (definition.getTransitTo() == null && definition.getAction() == null) {
                throw new ScreenRegistryException(String
                        .format("At least one property transitTo or action must be defined [button: %s]",
                                definition.getName()));
            }
            if (definition.getTransitTo() != null
                    && !screens.containsKey(definition.getTransitTo())) {
                throw new ScreenRegistryException(String.format("Screen '%s' which defined in transitTo does not exist [button: %s]",
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
        Objects.requireNonNull(screens, "screens is null");

        for(ScreenDefinition screen : screens.values()) {
            validateScreen(screen);
        }
    }

}
