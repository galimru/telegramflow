package org.telegram.telegramflow.services;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Properties;

public class DefaultMessageProvider implements MessageProvider {

    private final static String MESSAGES_FILENAME = "/messages.properties";

    private Properties properties;

    public DefaultMessageProvider() {
        properties = new Properties();
        InputStream is = getClass().getResourceAsStream(MESSAGES_FILENAME);
        if (is != null) {
            Reader reader = new InputStreamReader(is, Charset.defaultCharset());
            try {
                properties.load(reader);
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Cannot load messages from %s",
                        MESSAGES_FILENAME));
            }
        }
    }

    @Nonnull
    @Override
    public String getMessage(@Nonnull String key) {
        Objects.requireNonNull(key, "key is null");

        return properties.getProperty(key, key);
    }

    @Nonnull
    @Override
    public String formatMessage(@Nonnull String key, Object... args) {
        Objects.requireNonNull(key, "key is null");

        return String.format(properties.getProperty(key, key), args);
    }
}
