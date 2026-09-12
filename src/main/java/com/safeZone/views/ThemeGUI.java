package com.safeZone.views;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.table.Table;

public class ThemeGUI extends DefaultTheme {

    private final TextColor accent = TextColor.ANSI.CYAN;
    private final TextColor accentBright = TextColor.ANSI.CYAN_BRIGHT;
    private final TextColor bg = TextColor.ANSI.BLACK;
    private final TextColor fg = TextColor.ANSI.WHITE;
    private final TextColor muted = TextColor.ANSI.WHITE_BRIGHT;

    @Override
    public ThemeDefinition getWindowDefinition(Window window) {
        ThemeDefinition def = ThemeDefinition.builder()
            .normal(bg, fg)
            .build();
        return def;
    }

    @Override
    public ThemeDefinition getTextBoxDefinition(TextBox textBox) {
        return ThemeDefinition.builder()
            .normal(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK)
            .active(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE)
            .preSelected(TextColor.ANSI.WHITE, TextColor.ANSI.BLUE)
            .build();
    }

    @Override
    public ThemeDefinition getButtonDefinition(Button button) {
        return ThemeDefinition.builder()
            .noral(TextColor.ANSI.WHITE, bg)
            .active(TextColor.ANSI.BLACK, accent)
            .preSelected(TextColor.ANSI.WHITE, TextColor.ANSI.BLUE)
            .selected(TextColor.ANSI.BLACK, accentBright)
            .build();
    }

    @Override
    public ThemeDefinition getLabelDefinition(Label label) {
        return ThemeDefinition.builder()
            .normal(fg, bg)
            .build();
    }

    @Override
    public ThemeDefinition getPanelDefinition(Panel panel) {
        return ThemeDefinition.builder()
            .normal(fg, bg)
            .build();
    }

    @Override
    public ThemeDefinition getTableDefinition(Table<?> table) {
        return ThemeDefinition.builder()
            .normal(fg, bg)
            .active(TextColor.ANSI.BLACK, accentBright)
            .selected(TextColor.ANSI.BLACK, accent)
            .build();
    }
}
