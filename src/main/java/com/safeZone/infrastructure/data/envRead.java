package com.safezone.infrastructure.data;

import java.io.InputStream;
import java.util.Properties;

public class envRead {

    public static Properties readEnv() {
        Properties props = new Properties();
        try (InputStream is = envRead.class
                .getClassLoader()
                .getResourceAsStream(".env")) {

            if (is == null) {
                throw new IllegalStateException("Не найден .env в classpath");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении .env", e);
        }
        return props;
    }
}
