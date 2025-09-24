-- Script SQL para crear usuarios de prueba en FleetGuard360
-- Ejecutar después de que Spring Boot haya creado las tablas automáticamente



USE fleetguard360;

-- Verificar que las tablas existen
SHOW TABLES;

-- 1. Insertar roles básicos
INSERT IGNORE INTO roles (id, name) VALUES 
(1, 'ADMIN'),
(2, 'USER'),
(3, 'FLEET_MANAGER');


-- 2. Insertar usuarios de prueba
-- Nota: Las contraseñas están encriptadas con BCrypt
-- Contraseña original para todos: "password123"
-- Hash BCrypt: $2a$10$Xl0yhvzLIaJCDdKBS.jrduKxn8B5HDzOLMFsODgNyFLPu0rHOdHu2

INSERT IGNORE INTO users (id, username, password, enabled, failed_attempts, lock_time) VALUES 
(1, 'admin',  '$2a$10$Xl0yhvzLIaJCDdKBS.jrduKxn8B5HDzOLMFsODgNyFLPu0rHOdHu2',  true, 0, NULL),
(2, 'jperez',  '$2a$10$Xl0yhvzLIaJCDdKBS.jrduKxn8B5HDzOLMFsODgNyFLPu0rHOdHu2',  true, 0, NULL),
(3, 'mgarcia',  '$2a$10$Xl0yhvzLIaJCDdKBS.jrduKxn8B5HDzOLMFsODgNyFLPu0rHOdHu2',  true, 0, NULL),
(4, 'crodriguez',  '$2a$10$Xl0yhvzLIaJCDdKBS.jrduKxn8B5HDzOLMFsODgNyFLPu0rHOdHu2',  false, 0, NULL),
(5, 'locked_user',  '$2a$10$Xl0yhvzLIaJCDdKBS.jrduKxn8B5HDzOLMFsODgNyFLPu0rHOdHu2',  true, 3, NOW());

SELECT * FROM fleetguard360.users;


-- 3. Asignar roles a usuarios
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
-- admin: ADMIN y USER
(1, 1),
(1, 2),
-- jperez: FLEET_MANAGER y USER  
(2, 3),
(2, 2),
-- mgarcia: solo USER
(3, 2),
-- crodriguez: ADMIN pero deshabilitado
(4, 1),
-- locked_user: USER pero bloqueado
(5, 2);


-- 4. Insertar algunos registros de historial de ejemplo
INSERT IGNORE INTO login_history (id, user_id, login_time, ip_address, success) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), '192.168.1.100', true),
(2, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), '192.168.1.101', true),
(3, 3, DATE_SUB(NOW(), INTERVAL 30 MINUTE), '192.168.1.102', false),
(4, 5, NOW(), '192.168.1.103', false);

-- 5. Consultas de verificación
SELECT 'ROLES CREADOS:' as info;
SELECT id, name FROM roles;


SELECT 'USUARIOS CREADOS:' as info;
SELECT id, username,  enabled, failed_attempts, lock_time FROM users;

SELECT 'ASIGNACIÓN DE ROLES:' as info;
SELECT u.username, r.name as role_name, u.enabled, u.failed_attempts
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
ORDER BY u.username, r.name;


SELECT 'HISTORIAL DE ACCESOS:' as info;
SELECT u.username, lh.login_time, lh.ip_address, lh.success 
FROM login_history lh 
JOIN users u ON lh.user_id = u.id 
ORDER BY lh.login_time DESC;