package com.safeZone.model;
public class Bin {

    private int binId;
    private int priceAtHour;
    private int posX;
    private int posY;
    private String size;
    private boolean status;

    public Bin(int binId, int priceAtHour, int posX, int posY, String size, boolean status) {
        this.binId = binId;
        this.priceAtHour = priceAtHour;
        this.posX = posX;
        this.posY = posY;
        this.size = size;
        this.status = status;
    }

    // геттеры и сеттеры дописать в случае надобности
    public int getBinId() {
        return binId;
    }

    public int getPriceAtHour() {
        return priceAtHour;
    }

    public String getBinSize() {
        return size;
    }

    public String getBinStatus() {
        return status ? "Open" : "Closed";
    }

    public String getBinNumber() {
        String number = "" + posX + posY;
        return number;
    }
}
