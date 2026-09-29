package com.safezone.domain.model;

public class Warehouse {

    private final int warehouseId;
    private String name;
    private String address;

    public Warehouse(
            int warehouseId,
            String name,
            String address
    ) {
        this.warehouseId = warehouseId;
        this.name = name;
        this.address = address;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}