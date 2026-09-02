package com.safeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;
import org.slf4j.*;


public class JsonReader {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(JsonReader.class);

    public JsonData readDataFromJson(String resourceName) throws IOException {
        log.info("Чтение JSON из ресурса: {}", resourceName);
        InputStream fileName = getClass().getClassLoader().getResourceAsStream(resourceName);

        if (fileName == null) {
            throw new IOException("РЕСУРС НЕ НАЙДЕН, ПРОВЕРЬТЕ КОРРЕКТНОСТЬ ИМЕНИ ФАЙЛА: " + resourceName);
        }

        log.info("Чтение завершено.");
        return objectMapper.readValue(fileName, JsonData.class);
    }
}
