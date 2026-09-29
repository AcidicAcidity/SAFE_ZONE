package com.safezone.domain.enums;

public enum CellSize {
    SMALL(1, 1),
    MEDIUM(2, 2),
    LARGE(4, 2);

    private final int width;
    private final int height;

    CellSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}