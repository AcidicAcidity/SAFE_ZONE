package com.safeZone.model;
public class Bin {

    private int Bin_ID;
    private int Price_One_Hour;
    private int pos_x;
    private int pos_y;
    private String size;
    private boolean status;

    public Bin(int Bin_ID, int Price_One_Hour, int pos_x, int pos_y, String size, boolean status) {
        this.Bin_ID = Bin_ID;
        this.Price = Price_One_Hour;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.size = size;
        this.status = status;
    }
    // геттеры и сеттеры дописать в случае надобности
}