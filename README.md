# KEYSTONE – Field Service Management Platform

KEYSTONE is a Field Service Management Platform developed using Spring Boot, React, and PostgreSQL.

## Project Overview

The platform manages customers, sites, work orders, technicians, parts usage, time logs, SLA tracking, status history, and management reporting.

## User Roles

The application supports four roles:

- Dispatcher
- Technician
- Manager/Admin
- Customer

Role-based access is enforced on the server side using JWT authentication.

## Technology Stack

### Backend
- Java 21
- Spring Boot
- Spring Security
- JWT Authentication
- BCrypt Password Hashing
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger / OpenAPI

### Frontend
- React
- TypeScript
- Vite

## Main Features

### Work Orders
- Create work orders
- Assign technicians
- Update work-order status
- View status history
- Search and pagination
- SLA due-date tracking
- Technician photo upload

### Work Order Lifecycle

```text
NEW
 ↓
ASSIGNED
 ↓
IN_PROGRESS
 ↓
ON_HOLD
 ↓
IN_PROGRESS
 ↓
COMPLETED
 ↓
CLOSED