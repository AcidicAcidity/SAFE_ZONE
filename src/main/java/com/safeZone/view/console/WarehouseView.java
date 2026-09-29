package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.Warehouse;

import java.util.List;

public class WarehouseView {

    private final SafeZoneService service;

    public WarehouseView(
            SafeZoneService service) {

        this.service = service;
    }

    public void showWarehouses() {

        System.out.println(
                "================================="
        );

        System.out.println(
                "              СКЛАДЫ"
        );

        System.out.println(
                "================================="
        );

        System.out.println();

        List<Warehouse> warehouses =
                service.getWarehouses();

        for (Warehouse warehouse : warehouses) {

            System.out.println(
                    "Склад №"
                            + warehouse.getWarehouseId()
            );

            System.out.println(
                    "Название: "
                            + warehouse.getName()
            );

            System.out.println(
                    "Адрес: "
                            + warehouse.getAddress()
            );

            System.out.println(
                    "---------------------------------"
            );
        }
    }
}