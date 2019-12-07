package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class JsonUtil {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Update fromFile(String file) throws IOException {
        InputStream is = JsonUtil.class.getResourceAsStream(file);
        Objects.requireNonNull(is, String.format("Cannot open json file %s", file));
        return OBJECT_MAPPER.readValue(is, Update.class);
    }
}
