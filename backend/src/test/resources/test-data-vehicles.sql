-- =================================================================
-- SCRIPT SQL PARA DATOS DE PRUEBA - VEHÍCULOS FLEETGUARD360
-- =================================================================
-- Descripción: Script para insertar datos de prueba en la base de datos
-- Autor: Sistema FleetGuard360
-- Fecha: 2025-09-24
-- =================================================================

USE fleetguard360;

-- Insertar roles básicos si no existen
INSERT IGNORE INTO roles (name, description) VALUES 
('ADMIN', 'Administrador del sistema'),
('USER', 'Usuario regular'),
('DRIVER', 'Conductor de vehículos');

-- Insertar usuarios de prueba (contraseñas encriptadas con BCrypt)
-- Contraseña original: admin123, user123, driver123
INSERT IGNORE INTO users (username, password, email, enabled, failed_attempts, created_at) VALUES
('admin', '$2a$10$rNq2/sY8VDy/rGq.2pA3b.KVq6P2ZXVq6mP9wZ7zKjy3gUl9hF8TC', 'admin@fleetguard360.com', 1, 0, NOW()),
('user1', '$2a$10$rNq2/sY8VDy/rGq.2pA3b.KVq6P2ZXVq6mP9wZ7zKjy3gUl9hF8TC', 'user1@fleetguard360.com', 1, 0, NOW()),
('driver1', '$2a$10$rNq2/sY8VDy/rGq.2pA3b.KVq6P2ZXVq6mP9wZ7zKjy3gUl9hF8TC', 'driver1@fleetguard360.com', 1, 0, NOW()),
('driver2', '$2a$10$rNq2/sY8VDy/rGq.2pA3b.KVq6P2ZXVq6mP9wZ7zKjy3gUl9hF8TC', 'driver2@fleetguard360.com', 1, 0, NOW());

-- Asignar roles a usuarios
INSERT IGNORE INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN';

INSERT IGNORE INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'user1' AND r.name = 'USER';

INSERT IGNORE INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'driver1' AND r.name = 'DRIVER';

INSERT IGNORE INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'driver2' AND r.name = 'DRIVER';

-- =================================================================
-- DATOS DE PRUEBA - VEHÍCULOS
-- =================================================================

-- Insertar vehículos de diferentes tipos y estados
INSERT IGNORE INTO vehicles (
    license_plate, model, brand, year, capacity, status, fuel_type, 
    mileage, color, notes, created_at, updated_at, created_by
) VALUES 
-- Buses disponibles
('ABC123', 'Bus Urbano 2020', 'Mercedes-Benz', 2020, 45, 'AVAILABLE', 'DIESEL', 75000, 'Blanco', 'Bus principal para rutas urbanas', NOW(), NOW(), 'admin'),
('DEF456', 'Sprinter Ejecutiva', 'Mercedes-Benz', 2021, 16, 'AVAILABLE', 'DIESEL', 42000, 'Negro', 'Transporte ejecutivo empresarial', NOW(), NOW(), 'admin'),
('GHI789', 'Coaster Turística', 'Toyota', 2019, 25, 'AVAILABLE', 'GASOLINE', 68000, 'Blanco', 'Ideal para turismo y viajes largos', NOW(), NOW(), 'admin'),

-- Vehículos en uso
('JKL012', 'Hiace Super Grandia', 'Toyota', 2022, 14, 'IN_USE', 'GASOLINE', 15000, 'Gris', 'Van familiar de alta gama', NOW(), NOW(), 'admin'),
('MNO345', 'Iveco Daily', 'Iveco', 2020, 20, 'IN_USE', 'DIESEL', 89000, 'Rojo', 'Transporte mediano para grupos', NOW(), NOW(), 'admin'),

-- Vehículos en mantenimiento
('PQR678', 'Bus Metropolitano', 'Volvo', 2018, 60, 'MAINTENANCE', 'DIESEL', 125000, 'Azul', 'En mantenimiento programado - revisar frenos', NOW(), NOW(), 'admin'),
('STU901', 'Master Passenger', 'Renault', 2019, 12, 'MAINTENANCE', 'DIESEL', 95000, 'Blanco', 'Reparación sistema eléctrico', NOW(), NOW(), 'admin'),

-- Vehículos fuera de servicio
('VWX234', 'Sprinter Clásica', 'Mercedes-Benz', 2017, 19, 'OUT_OF_SERVICE', 'DIESEL', 180000, 'Plateado', 'Requiere reparación mayor del motor', NOW(), NOW(), 'admin'),

-- Vehículos eléctricos e híbridos (más modernos)
('YZA567', 'eCrafter Eléctrico', 'Volkswagen', 2023, 8, 'AVAILABLE', 'ELECTRIC', 5000, 'Blanco', 'Vehículo 100% eléctrico para rutas cortas', NOW(), NOW(), 'admin'),
('BCD890', 'Prius Hybrid Van', 'Toyota', 2024, 7, 'AVAILABLE', 'HYBRID', 2000, 'Verde', 'Transporte ecológico híbrido', NOW(), NOW(), 'admin'),

-- Vehículos con diferentes capacidades
('EFG123', 'Microbus Escolar', 'Chevrolet', 2020, 35, 'AVAILABLE', 'GASOLINE', 55000, 'Amarillo', 'Transporte estudiantil seguro', NOW(), NOW(), 'admin'),
('HIJ456', 'Van Ejecutiva', 'Ford', 2021, 8, 'AVAILABLE', 'GASOLINE', 32000, 'Negro', 'Transporte VIP para ejecutivos', NOW(), NOW(), 'admin'),
('KLM789', 'Bus Articulado', 'Scania', 2019, 80, 'AVAILABLE', 'DIESEL', 95000, 'Rojo', 'Bus de alta capacidad para rutas principales', NOW(), NOW(), 'admin'),

-- Vehículos más antiguos
('NOP012', 'Bus Clásico', 'Mercedes-Benz', 2016, 40, 'AVAILABLE', 'DIESEL', 200000, 'Azul', 'Bus confiable para rutas secundarias', NOW(), NOW(), 'admin'),
('QRS345', 'Van Familiar', 'Nissan', 2018, 12, 'AVAILABLE', 'GASOLINE', 85000, 'Plata', 'Transporte familiar cómodo y espacioso', NOW(), NOW(), 'admin');

-- =================================================================
-- CONSULTAS DE VERIFICACIÓN
-- =================================================================

-- Verificar usuarios creados
SELECT u.username, u.email, u.enabled, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
ORDER BY u.username;

-- Verificar vehículos por estado
SELECT status, COUNT(*) as cantidad 
FROM vehicles 
GROUP BY status 
ORDER BY cantidad DESC;

-- Verificar vehículos por tipo de combustible
SELECT fuel_type, COUNT(*) as cantidad 
FROM vehicles 
GROUP BY fuel_type 
ORDER BY cantidad DESC;

-- Verificar capacidades
SELECT 
    CASE 
        WHEN capacity <= 10 THEN 'Pequeño (1-10)'
        WHEN capacity <= 20 THEN 'Mediano (11-20)'
        WHEN capacity <= 40 THEN 'Grande (21-40)'
        ELSE 'Muy Grande (40+)'
    END as categoria_capacidad,
    COUNT(*) as cantidad
FROM vehicles 
GROUP BY 
    CASE 
        WHEN capacity <= 10 THEN 'Pequeño (1-10)'
        WHEN capacity <= 20 THEN 'Mediano (11-20)'
        WHEN capacity <= 40 THEN 'Grande (21-40)'
        ELSE 'Muy Grande (40+)'
    END
ORDER BY cantidad DESC;

-- Listado completo de vehículos
SELECT 
    license_plate as Placa,
    CONCAT(brand, ' ', model) as Vehiculo,
    year as Año,
    capacity as Capacidad,
    status as Estado,
    fuel_type as Combustible,
    color as Color
FROM vehicles 
WHERE status != 'INACTIVE'
ORDER BY license_plate;