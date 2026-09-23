package com.safeZone.views;

import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.graphics.Theme;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ThemeGUI {

    private static final String THEME_RESOURCE = "/theme/themeTUI.properties";

    public static Theme build() {
        Properties properties = new Properties();
        try (InputStream stream = ThemeGUI.class.getResourceAsStream(THEME_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Файл темы не найден: " + THEME_RESOURCE);
            }
            properties.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить тему", e);
        }

        return new PropertyTheme(properties, true);
    }
}
