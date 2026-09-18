CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255)
);

CREATE TABLE sites (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    customer_id BIGINT NOT NULL,
    CONSTRAINT fk_sites_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE parts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(255),
    unit_cost DOUBLE PRECISION NOT NULL,
    stock_qty INTEGER NOT NULL
);

CREATE TABLE work_orders (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    sla_due_at TIMESTAMP,
    customer_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    assigned_to BIGINT,
    created_at TIMESTAMP,
    photo_url VARCHAR(255),

    CONSTRAINT fk_work_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),

    CONSTRAINT fk_work_orders_site
        FOREIGN KEY (site_id) REFERENCES sites(id),

    CONSTRAINT fk_work_orders_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE TABLE part_usage (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    part_id BIGINT NOT NULL,
    qty_used INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    total_cost DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_part_usage_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders(id),

    CONSTRAINT fk_part_usage_part
        FOREIGN KEY (part_id) REFERENCES parts(id)
);

CREATE TABLE time_logs (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    minutes INTEGER NOT NULL,
    note VARCHAR(255),

    CONSTRAINT fk_time_logs_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders(id),

    CONSTRAINT fk_time_logs_technician
        FOREIGN KEY (technician_id) REFERENCES users(id)
);

CREATE TABLE work_order_status_history (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by BIGINT,
    changed_at TIMESTAMP,
    note VARCHAR(255),

    CONSTRAINT fk_status_history_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders(id),

    CONSTRAINT fk_status_history_changed_by
        FOREIGN KEY (changed_by) REFERENCES users(id)
);