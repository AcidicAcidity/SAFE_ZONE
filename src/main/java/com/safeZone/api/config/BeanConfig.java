package com.safezone.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.safezone.infrastructure.data.DBHelper;
import com.safezone.infrastructure.data.repository.*;
import com.safezone.application.service.PasswordService;
import com.safezone.application.service.SafeZoneService;

@Configuration
public class BeanConfig {

    @Bean
    public DBHelper dbHelper() {
        return new DBHelper();
    }

    @Bean
    public CellRepository cellRepository(DBHelper db) {
        return new PostgresCellRepository(db);
    }

    @Bean
    public RentalRepository rentalRepository(DBHelper db) {
        return new PostgresRentalRepository(db);
    }

    @Bean
    public UserRepository userRepository(DBHelper db) {
        return new PostgresUserRepository(db);
    }

    @Bean
    public WarehouseRepository warehouseRepository(DBHelper db) {
        return new PostgresWarehouseRepository(db);
    }

    @Bean
    public PasswordService passwordService() {
        return new PasswordService();
    }

    @Bean
    public SafeZoneService safeZoneService(UserRepository users, WarehouseRepository warehouses, CellRepository cells,
            RentalRepository rentals, PasswordService passwordService ) {
        return new SafeZoneService(users, warehouses, cells, rentals, passwordService);
    }
}
