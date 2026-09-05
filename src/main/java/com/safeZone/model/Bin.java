package com.safeZone.model;
public class Bin {

    private int ID;
    private int Price;
    private int pos_x;
    private int pos_y;
    private String size;
    private boolean status;

    public Bin(int ID, int Price, int pos_x, int pos_y, String size, boolean status) {
        this.ID = ID;
        this.Price = Price;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.size = size;
        this.status = status;
    }
    // геттеры и сеттеры дописать в случае надобности
}