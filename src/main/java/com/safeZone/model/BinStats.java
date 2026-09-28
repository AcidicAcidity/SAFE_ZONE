package com.safeZone.model;

public class BinStats {
    private final int binNumber;
    private final String currentStatus;
    private final int rentCount;
    private final int longestRentHours;

    public BinStats(int binNumber, String currentStatus, int rentCount, int longestRentHours) {
        this.binNumber = binNumber;
        this.currentStatus = currentStatus;
        this.rentCount = rentCount;
        this.longestRentHours = longestRentHours;
    }

    public int getBinNumber() { return binNumber; }
    public String getCurrentStatus() { return currentStatus; }
    public int getRentCount() { return rentCount; }
    public int getLongestRentHours() { return longestRentHours; }
}
