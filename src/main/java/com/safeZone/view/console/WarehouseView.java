package com.safezone.view.console;

import com.safezone.backend.application.service.WarehouseService;
import com.safezone.backend.domain.model.Warehouse;

import java.util.List;

public class WarehouseView {

    private final WarehouseService warehouseService;

    public WarehouseView(
            WarehouseService warehouseService) {

        this.warehouseService = warehouseService;
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
                warehouseService.getWarehouses();

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
