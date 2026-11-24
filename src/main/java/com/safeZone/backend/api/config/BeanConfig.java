package com.safezone.backend.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import com.safezone.backend.infrastructure.data.DBHelper;
import com.safezone.backend.infrastructure.data.DatabaseInitializer;
import com.safezone.backend.infrastructure.data.repository.*;
import com.safezone.backend.application.service.*;

@Configuration
public class BeanConfig {

    @Bean
    public DBHelper dbHelper(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String user,
            @Value("${spring.datasource.password}") String password) {
        return new DBHelper(url, user, password);
    }

    @Bean(initMethod = "initialize")
    public DatabaseInitializer databaseInitializer(DBHelper database,
            @Value("${ADMIN_LOGIN}") String login,
            @Value("${ADMIN_PASSWORD}") String password) {
        return new DatabaseInitializer(database, login, password);
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
    public CellService cellService(CellRepository cellRepository, RentalRepository rentalRepository) {
        return new CellService(cellRepository, rentalRepository);
    }

    @Bean
    public AuthService authService(UserRepository userRepository, PasswordService passwordService) {
        return new AuthService(userRepository, passwordService);
    }

    @Bean
    public RentalService rentalService(RentalRepository rentalRepository, CellService cellService) {
        return new RentalService(rentalRepository, cellService);
    }

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public WarehouseService warehouseService(WarehouseRepository warehouseRepository) {
        return new WarehouseService(warehouseRepository);
    }
}
