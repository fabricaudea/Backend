# FleetGuard360

FleetGuard360 es una plataforma de gestión de flota desarrollada como parte de la Fábrica Escuela. Permite el monitoreo y administración de vehículos con control de acceso basado en roles.

## 🚀 Características

- **Autenticación por roles**: Administrador y Operador con diferentes niveles de acceso
- **Gestión de vehículos**: CRUD completo para administradores, vista de solo lectura para operadores
- **Panel de alertas**: Monitoreo de eventos y alertas de la flota
- **Reportes**: Análisis y métricas para administradores
- **Configuración**: Panel de configuración del sistema

## 👥 Roles de Usuario

### Administrador
- Acceso completo a gestión de flota (crear, editar, eliminar vehículos)
- Acceso a reportes y análisis
- Configuración del sistema
- Panel de alertas

### Operador
- Vista de solo lectura de la flota
- Panel de alertas principal
- Sin acceso a reportes ni configuración

## 🔐 Credenciales de Prueba

Para probar la aplicación, utiliza las siguientes credenciales:

### Administrador
- **Usuario**: `admin`
- **Contraseña**: `admin123`

### Operador
- **Usuario**: `operador`
- **Contraseña**: `op123`

## 🛠️ Tecnologías

- **Frontend**: React + TypeScript
- **Styling**: TailwindCSS + shadcn/ui
- **Routing**: React Router
- **Estado**: Context API
- **Build**: Vite

## 🎯 Flujo de Uso

1. **Iniciar sesión** con las credenciales proporcionadas
2. **Administrador**: Será redirigido a `/fleet` con acceso completo
3. **Operador**: Será redirigido a `/alerts` con acceso limitado
4. Navegar entre las secciones según los permisos del rol
5. Intentar acceder a rutas restringidas mostrará página 403

## 🔄 Estados de la Aplicación

- **Loading**: Spinners durante operaciones
- **Empty**: Estados vacíos con llamadas a la acción
- **Error**: Manejo de errores con toasts informativos
- **Success**: Confirmaciones de operaciones exitosas

## ♿ Accesibilidad

- Labels visibles en todos los inputs
- Navegación por teclado
- Contraste mínimo 4.5:1
- Tooltips informativos para acciones restringidas
- Mensajes claros para lectores de pantalla

## 🚧 Funcionalidades Futuras

La aplicación está preparada para conectarse a una API REST con las siguientes funciones:
- `listVehicles()`
- `createVehicle(data)`
- `updateVehicle(id, data)`
- `deleteVehicle(id)`

Actualmente utiliza datos simulados (mock) para demostración.

---

Desarrollado para la Fábrica Escuela - FleetGuard360 🚛
