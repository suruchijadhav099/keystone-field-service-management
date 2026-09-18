CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (name, email, password_hash, role)
VALUES
(
    'Seed Dispatcher',
    'dispatcher@keystone.com',
    crypt('Dispatcher@123', gen_salt('bf')),
    'DISPATCHER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password_hash, role)
VALUES
(
    'Seed Manager',
    'manager@keystone.com',
    crypt('Manager@123', gen_salt('bf')),
    'MANAGER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password_hash, role)
VALUES
(
    'Seed Technician',
    'technician@keystone.com',
    crypt('Technician@123', gen_salt('bf')),
    'TECHNICIAN'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password_hash, role)
VALUES
(
    'Seed Customer',
    'customer@keystone.com',
    crypt('Customer@123', gen_salt('bf')),
    'CUSTOMER'
)
ON CONFLICT (email) DO NOTHING;