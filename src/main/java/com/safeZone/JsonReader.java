package com.safeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;


public class JsonReader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonData readDataFromJson(String resourceName) throws IOException {
        InputStream fileName = getClass().getClassLoader().getResourceAsStream(resourceName);

        if (fileName == null) {
            throw new IOException("РЕСУРС НЕ НАЙДЕН, ПРОВЕРЬТЕ КОРРЕКТНОСТЬ ИМЕНИ ФАЙЛА: " + resourceName);
        }

        return objectMapper.readValue(fileName, JsonData.class);
    }
}
