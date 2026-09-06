package com.safeZone.model;
public class Payment {
    private int Order_ID;
    private Bin bin_id;
    private Bin price_one_hour;
    private Status status;
    private LocalDateTime сreated_at;
    private LocalDateTime updated_at;
    private LocalDateTime end_rent_date;
    private User User_ID;
    public enum Status {
        
    }
}