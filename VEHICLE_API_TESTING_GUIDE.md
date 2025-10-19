# 🚛 Guía de Testing - API de Vehículos FleetGuard360

## 📋 Índice
1. [Preparación del Entorno](#preparación-del-entorno)
2. [Autenticación](#autenticación)
3. [Testing HU: Dar de Alta Vehículos](#testing-hu-dar-de-alta-vehículos)
4. [Testing HU: Editar Vehículos](#testing-hu-editar-vehículos)
5. [Testing HU: Dar de Baja Vehículos](#testing-hu-dar-de-baja-vehículos)
6. [Endpoints Adicionales](#endpoints-adicionales)
7. [Scripts de Postman](#scripts-de-postman)

---

## 🔧 Preparación del Entorno

### 1. Configurar Base de Datos
```bash
# Ejecutar el script SQL de datos de prueba
mysql -u fleetguard_user -p fleetguard360 < src/test/resources/test-data-vehicles.sql
```

### 2. Iniciar Aplicación
```bash
cd backend
./mvnw spring-boot:run
```

### 3. URL Base
```
http://localhost:8080/api
```

---

## 🔐 Autenticación

### Login como Administrador
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' \
  -c cookies.txt
```

**Respuesta esperada:**
```json
{
  "message": "Login exitoso",
  "username": "admin",
  "roles": ["ADMIN"],
  "loginTime": "2025-09-24T10:30:45",
  "status": "SUCCESS"
}
```

### Verificar Estado de Autenticación
```bash
curl -X GET http://localhost:8080/api/auth/status \
  -b cookies.txt
```

---

## 🚗 Testing HU: Dar de Alta Vehículos

### ✅ Caso de Éxito - Crear Vehículo Válido
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "licensePlate": "XYZ999",
    "model": "Bus Urbano Moderno",
    "brand": "Mercedes-Benz",
    "year": 2024,
    "capacity": 50,
    "status": "AVAILABLE",
    "fuelType": "DIESEL",
    "mileage": 0,
    "color": "Blanco",
    "notes": "Vehículo nuevo para testing"
  }'
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 16,
  "licensePlate": "XYZ999",
  "model": "Bus Urbano Moderno",
  "brand": "Mercedes-Benz",
  "year": 2024,
  "capacity": 50,
  "status": "AVAILABLE",
  "statusDisplayName": "Disponible",
  "fuelType": "DIESEL",
  "fuelTypeDisplayName": "Diésel",
  "mileage": 0,
  "color": "Blanco",
  "notes": "Vehículo nuevo para testing",
  "createdAt": "2025-09-24T10:35:22",
  "updatedAt": "2025-09-24T10:35:22",
  "createdBy": "admin"
}
```

### ❌ Caso de Error - Placa Duplicada
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "licensePlate": "ABC123",
    "model": "Bus Duplicado",
    "capacity": 30,
    "status": "AVAILABLE"
  }'
```

**Respuesta esperada (409 Conflict):**
```json
{
  "errorCode": "DUPLICATE_RESOURCE",
  "message": "Ya existe un vehículo con la placa: ABC123",
  "status": 409,
  "timestamp": "2025-09-24T10:40:15"
}
```

### ❌ Caso de Error - Validación de Campos
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "licensePlate": "",
    "model": "A",
    "capacity": 0,
    "status": "AVAILABLE"
  }'
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Errores de validación...",
  "status": 400,
  "fieldErrors": {
    "licensePlate": "La placa es obligatoria",
    "model": "El modelo debe tener entre 2 y 50 caracteres",
    "capacity": "La capacidad debe ser mayor a 0"
  }
}
```

### ✅ Verificar que Aparece en la Lista
```bash
curl -X GET http://localhost:8080/api/vehicles \
  -b cookies.txt
```

---

## ✏️ Testing HU: Editar Vehículos

### ✅ Obtener Vehículo por ID
```bash
curl -X GET http://localhost:8080/api/vehicles/1 \
  -b cookies.txt
```

### ✅ Actualizar Vehículo Existente
```bash
curl -X PUT http://localhost:8080/api/vehicles/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "licensePlate": "ABC123",
    "model": "Bus Urbano 2020 - ACTUALIZADO",
    "brand": "Mercedes-Benz",
    "year": 2020,
    "capacity": 48,
    "status": "MAINTENANCE",
    "fuelType": "DIESEL",
    "mileage": 75500,
    "color": "Azul",
    "notes": "Vehículo actualizado - mantenimiento programado"
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "id": 1,
  "licensePlate": "ABC123",
  "model": "Bus Urbano 2020 - ACTUALIZADO",
  "capacity": 48,
  "status": "MAINTENANCE",
  "statusDisplayName": "En Mantenimiento",
  "updatedAt": "2025-09-24T10:45:30",
  "updatedBy": "admin"
}
```

### ❌ Caso de Error - Vehículo No Existe
```bash
curl -X PUT http://localhost:8080/api/vehicles/999 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "licensePlate": "NEW999",
    "model": "Modelo Test",
    "capacity": 20,
    "status": "AVAILABLE"
  }'
```

**Respuesta esperada (404 Not Found):**
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Vehículo no encontrado con ID: 999",
  "status": 404
}
```

### ✅ Verificar Cambios Reflejados
```bash
curl -X GET http://localhost:8080/api/vehicles/1 \
  -b cookies.txt
```

---

## 🗑️ Testing HU: Dar de Baja Vehículos

### ❌ Error - Vehículo en Uso
```bash
curl -X DELETE http://localhost:8080/api/vehicles/4 \
  -b cookies.txt
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "errorCode": "BUSINESS_RULE_VIOLATION",
  "message": "No se puede eliminar el vehículo JKL012 porque está actualmente en uso. Debe finalizar los viajes en curso antes de eliminarlo.",
  "status": 400
}
```

### ✅ Eliminación Exitosa (Soft Delete)
```bash
curl -X DELETE http://localhost:8080/api/vehicles/2 \
  -b cookies.txt
```

**Respuesta esperada (200 OK):**
```json
{
  "message": "Vehículo eliminado exitosamente de la flota"
}
```

### ✅ Verificar que No Aparece en Lista de Activos
```bash
curl -X GET http://localhost:8080/api/vehicles \
  -b cookies.txt
```

### ❌ Error - Vehículo No Existe
```bash
curl -X DELETE http://localhost:8080/api/vehicles/999 \
  -b cookies.txt
```

---

## 📊 Endpoints Adicionales

### Listar Vehículos por Estado
```bash
# Disponibles
curl -X GET http://localhost:8080/api/vehicles/status/AVAILABLE \
  -b cookies.txt

# En uso
curl -X GET http://localhost:8080/api/vehicles/status/IN_USE \
  -b cookies.txt

# En mantenimiento
curl -X GET http://localhost:8080/api/vehicles/status/MAINTENANCE \
  -b cookies.txt
```

### Obtener Solo Vehículos Disponibles
```bash
curl -X GET http://localhost:8080/api/vehicles/available \
  -b cookies.txt
```

### Buscar por Placa
```bash
curl -X GET http://localhost:8080/api/vehicles/by-plate/ABC123 \
  -b cookies.txt
```

### Cambiar Estado de Vehículo
```bash
curl -X PATCH http://localhost:8080/api/vehicles/1/status \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "status": "AVAILABLE"
  }'
```

---

## 🔍 Validaciones de Criterios de Aceptación

### HU: Dar de Alta Vehículos
- ✅ **Criterio 1**: Datos obligatorios validados (placa, modelo, capacidad, estado)
- ✅ **Criterio 2**: Prevención de duplicados por placa
- ✅ **Criterio 3**: Vehículo aparece en lista tras registro

### HU: Editar Vehículos
- ✅ **Criterio 1**: Modificación de datos permitida
- ✅ **Criterio 2**: Cambios reflejados inmediatamente
- ✅ **Criterio 3**: Error al editar vehículo inexistente

### HU: Dar de Baja Vehículos
- ✅ **Criterio 1**: Vehículo desaparece de lista de disponibles
- ✅ **Criterio 2**: Advertencia si está en uso (no permite eliminación)
- ✅ **Criterio 3**: Confirmación de eliminación exitosa

---

## 🚀 Scripts para Postman

### Colección Postman
```json
{
  "info": {
    "name": "FleetGuard360 - Vehículos API",
    "description": "Testing completo de CRUD de vehículos"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/api"
    }
  ]
}
```

### Environment Variables
- `baseUrl`: http://localhost:8080/api
- `authToken`: (se obtiene del login)
- `vehicleId`: (ID del vehículo para testing)

---

## 🔧 Comandos de Utilidad

### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt
```

### Limpiar Cookies
```bash
rm cookies.txt
```

### Verificar Logs de Aplicación
```bash
tail -f logs/application.log
```

---

## 📝 Notas Importantes

1. **Roles**: Los endpoints de creación, edición y eliminación requieren rol ADMIN
2. **Autenticación**: Usar cookies o header Authorization según configuración
3. **Validaciones**: Todos los campos tienen validaciones específicas
4. **Estados**: Los vehículos en uso no pueden ser eliminados
5. **Soft Delete**: La eliminación es lógica (estado INACTIVE)

---

**¡Testing completo de las 3 Historias de Usuario implementado! 🎉**