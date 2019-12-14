package org.telegram.telegramflow.services.defaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegramflow.exceptions.CallbackException;
import org.telegram.telegramflow.handlers.CallbackAction;
import org.telegram.telegramflow.xml.actions.ActionIndex;
import org.telegram.telegramflow.xml.actions.ActionsDescriptor;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CallbackService {

    private Logger logger = LoggerFactory.getLogger(CallbackService.class);

    private final static String DEFAULT_DESCRIPTOR_PATH = "actions.xml";
    private final static String DELIMITER = "#";

    private String descriptorPath;

    private Map<String, CallbackAction> actions = new ConcurrentHashMap<>();

    public void initialize() throws CallbackException {
        logger.info("Initializing callback service");
        if (descriptorPath == null) {
            descriptorPath = DEFAULT_DESCRIPTOR_PATH;
        }
        logger.info("Loading action descriptor from path {}", descriptorPath);
        ActionsDescriptor descriptor = loadDescriptor(descriptorPath);
        if (descriptor.getActions() != null) {
            logger.info("Registering {} callback actions", descriptor.getActions().size());
            for (ActionIndex index : descriptor.getActions()) {
                register(index.getKey(), index.getActionClass());
                logger.info("Registered '{}' callback action definition", index.getKey());
            }
        }
    }

    public void setDescriptorPath(String descriptorPath) {
        this.descriptorPath = descriptorPath;
    }

    public void execute(Update update) throws CallbackException {
        if (!update.hasCallbackQuery()) {
            throw new CallbackException("Update doesn't have callback query");
        }

        String data = update.getCallbackQuery().getData();
        String[] tokens = data.split(DELIMITER);

        String key = tokens[0];
        String value = tokens.length == 2 ? tokens[1] : null;

        getAction(key).execute(update, value);
    }

    @Nonnull
    public CallbackAction getAction(@Nonnull String key) throws CallbackException {
        Objects.requireNonNull(actions, "key is null");

        CallbackAction action = actions.get(key);
        if (action == null) {
            throw new CallbackException(String.format("Action '%s' not found in registry", key));
        }
        return action;
    }

    public int size() {
        return actions.size();
    }

    private void register(String key,  Class<? extends CallbackAction> actionClass) throws CallbackException {
        Objects.requireNonNull(key, "key is null");
        Objects.requireNonNull(actionClass, "actionClass is null");

        CallbackAction action = createAction(actionClass);
        actions.put(key, action);
    }

    private ActionsDescriptor loadDescriptor(String file) throws CallbackException {
        Objects.requireNonNull(file, "file is null");

        try {
            JAXBContext context = JAXBContext.newInstance(ActionsDescriptor.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = getClass().getClassLoader().getResourceAsStream(file);
            if (is == null) {
                throw new CallbackException(String.format("Actions descriptor '%s' does not exist", file));
            }
            return (ActionsDescriptor) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new CallbackException(String.format("Cannot parse actions descriptor [%s]", file), e);
        }
    }

    private CallbackAction createAction(Class<? extends CallbackAction> actionClass) throws CallbackException {
        Objects.requireNonNull(actionClass, "actionClass is null");

        try {
            Constructor constructor = actionClass.getConstructor();
            return (CallbackAction) constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException
                | IllegalAccessException | NoSuchMethodException e) {
            throw new CallbackException("Cannot create action " + actionClass.getName(), e);
        }
    }

}
