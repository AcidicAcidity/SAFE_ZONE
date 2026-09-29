CREATE TABLE IF NOT EXISTS users (
                                     user_id SERIAL PRIMARY KEY,
                                     login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',

    CONSTRAINT users_status_check
    CHECK (status IN ('ACTIVE', 'BLOCKED', 'DELETED')),

    CONSTRAINT users_role_check
    CHECK (role IN ('CLIENT', 'ADMIN'))
    );


CREATE TABLE IF NOT EXISTS warehouses (
                                          warehouse_id SERIAL PRIMARY KEY,
                                          name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL
    );


CREATE TABLE IF NOT EXISTS cells (
                                     cell_id SERIAL PRIMARY KEY,
                                     warehouse_id INTEGER NOT NULL,
                                     row_num INTEGER NOT NULL,
                                     column_num INTEGER NOT NULL,
                                     number VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    CONSTRAINT cells_warehouse_fk
    FOREIGN KEY (warehouse_id)
    REFERENCES warehouses(warehouse_id),

    CONSTRAINT cells_size_check
    CHECK (size IN ('SMALL', 'MEDIUM', 'LARGE')),

    CONSTRAINT cells_status_check
    CHECK (status IN ('AVAILABLE', 'RENTED', 'UNAVAILABLE'))
    );


CREATE TABLE IF NOT EXISTS rentals (
                                       rental_id SERIAL PRIMARY KEY,
                                       user_id INTEGER NOT NULL,
                                       cell_id INTEGER NOT NULL,
                                       start_date_time TIMESTAMP NOT NULL,
                                       end_date_time TIMESTAMP NOT NULL,
                                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT rentals_user_fk
    FOREIGN KEY (user_id)
    REFERENCES users(user_id),

    CONSTRAINT rentals_cell_fk
    FOREIGN KEY (cell_id)
    REFERENCES cells(cell_id),

    CONSTRAINT rentals_status_check
    CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),

    CONSTRAINT rentals_dates_check
    CHECK (end_date_time > start_date_time)
    );