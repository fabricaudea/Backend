# Guía de Testing - FleetGuard360 Authentication API

## 📋 Usuarios de Prueba Disponibles

Todos los usuarios tienen la contraseña: **password123**

| Username      | Roles                | Estado      | Descripción                    |
|---------------|----------------------|-------------|--------------------------------|
| admin         | ADMIN, USER         | Activo      | Administrador del sistema      |
| jperez        | FLEET_MANAGER, USER | Activo      | Gestor de flota               |
| mgarcia       | USER                | Activo      | Usuario estándar              |
| crodriguez    | ADMIN               | Deshabilitado| Admin deshabilitado           |
| locked_user   | USER                | Bloqueado   | Usuario con 3 intentos fallidos|

---

## 🚀 Paso 1: Iniciar la aplicación

```bash
cd "c:\Users\andre\Documents\UDEA\2025-2\Gestión de proyectos de TI\Fabrica Escuela\app\FE_FleetGuard360\backend"
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

---

## 🔧 Paso 2: Ejecutar script de datos de prueba

1. Conectarse a MySQL:
```bash
mysql -u fleetguard_user -p fleetguard360
```

2. Ejecutar el script:
```bash
source test_data.sql
```

---

## 🧪 Paso 3: Tests con cURL

### 📍 Test 1: Login Exitoso - Usuario Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }' \
  -c cookies.txt -v
```

**Resultado esperado:** HTTP 200 + información del usuario y roles

### 📍 Test 2: Verificar Estado de Autenticación
```bash
curl -X GET http://localhost:8080/api/auth/status \
  -b cookies.txt -v
```

**Resultado esperado:** HTTP 200 + información del usuario autenticado

### 📍 Test 3: Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt -v
```

**Resultado esperado:** HTTP 200 + mensaje de logout exitoso

### 📍 Test 4: Login con Credenciales Incorrectas
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }' -v
```

**Resultado esperado:** HTTP 401 + mensaje de error

### 📍 Test 5: Login con Usuario Deshabilitado
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "crodriguez",
    "password": "password123"
  }' -v
```

**Resultado esperado:** HTTP 403 + mensaje "Cuenta deshabilitada"

### 📍 Test 6: Probar Bloqueo por 3 Intentos (Simular)
```bash
# Primer intento fallido
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mgarcia",
    "password": "wrong1"
  }' -v

# Segundo intento fallido
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mgarcia",
    "password": "wrong2"
  }' -v

# Tercer intento fallido (esto debería bloquear la cuenta)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mgarcia",
    "password": "wrong3"
  }' -v

# Cuarto intento con contraseña correcta (debería estar bloqueado)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mgarcia",
    "password": "password123"
  }' -v
```

**Resultado esperado:** HTTP 423 (Locked) + mensaje de cuenta bloqueada

### 📍 Test 7: Validación de Entrada
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "",
    "password": "123"
  }' -v
```

**Resultado esperado:** HTTP 400 + mensajes de validación

---

## 🐛 Consultas SQL para Monitoreo

### Ver intentos fallidos y bloqueos:
```sql
SELECT username, failed_attempts, lock_time, enabled 
FROM users 
WHERE failed_attempts > 0 OR lock_time IS NOT NULL;
```

### Ver historial de login:
```sql
SELECT u.username, lh.login_time, lh.ip_address, lh.success 
FROM login_history lh 
JOIN users u ON lh.user_id = u.id 
ORDER BY lh.login_time DESC 
LIMIT 10;
```

### Desbloquear usuario manualmente (si es necesario):
```sql
UPDATE users 
SET failed_attempts = 0, lock_time = NULL 
WHERE username = 'mgarcia';
```

---

## 📊 Verificación de Criterios de Aceptación

✅ **Criterio 1:** Validación usuario/contraseña → Test 1, 4  
✅ **Criterio 2:** Bloqueo tras 3 intentos → Test 6  
✅ **Criterio 3:** Acceso basado en roles → Test 2, 5  
✅ **Criterio 4:** Registro de historial → Consulta SQL historial  

---

## 🔍 Logs a Monitorear

Revisar en la consola de Spring Boot:
- Intentos de login exitosos/fallidos
- Bloqueos de cuenta
- IPs de origen
- Eventos de autenticación