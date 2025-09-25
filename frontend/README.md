# FleetGuard360

FleetGuard360 es una plataforma de gestión de flota desarrollada como parte de la Fábrica Escuela. Sistema completo de monitoreo y administración de vehículos con control de acceso basado en roles (RBAC), diseñado para ser escalable, accesible y mantenible.

## 🚀 Características Principales

- **🔐 Autenticación por roles**: Sistema RBAC con Administrador y Operador
- **🚛 Gestión de vehículos**: CRUD completo con validaciones y estados
- **📊 Panel de alertas**: Monitoreo en tiempo real de eventos de la flota
- **📈 Reportes**: Análisis y métricas para toma de decisiones
- **⚙️ Configuración**: Panel de administración del sistema
- **📱 Responsive**: Diseño adaptativo para móvil, tablet y escritorio
- **♿ Accesibilidad**: Cumple estándares WCAG 2.1 AA

## 🏗️ Arquitectura del Código

### Estructura de Directorios
```
src/
├── components/           # Componentes reutilizables
│   ├── ui/              # Componentes base de shadcn/ui
│   ├── Navigation.tsx   # Sidebar y navegación principal
│   ├── VehicleTable.tsx # Tabla de vehículos con CRUD
│   ├── VehicleForm.tsx  # Formulario modal para crear/editar
│   ├── ConfirmDialog.tsx# Diálogo de confirmación
│   └── ProtectedRoute.tsx# Protección de rutas por rol
├── contexts/            # Estado global con Context API
│   ├── AuthContext.tsx  # Autenticación y manejo de usuario
│   └── VehicleContext.tsx# Estado de vehículos y operaciones
├── pages/               # Páginas principales de la aplicación
│   ├── Login.tsx        # Pantalla de autenticación
│   ├── Fleet.tsx        # Gestión de flota (wrapper)
│   ├── Alerts.tsx       # Panel de alertas
│   ├── Reports.tsx      # Dashboard de reportes
│   ├── Settings.tsx     # Configuración del sistema
│   └── Forbidden.tsx    # Página 403 para acceso denegado
├── types/               # Definiciones de TypeScript
│   └── vehicle.ts       # Interfaces y tipos para vehículos
├── lib/                 # Utilidades y configuraciones
│   └── utils.ts         # Funciones helper y utilidades
└── hooks/               # Custom hooks reutilizables
    └── use-mobile.tsx   # Hook para detección de dispositivos móviles
```

### Patrones de Diseño Implementados

#### 1. **Context Pattern** - Gestión de Estado Global
```typescript
// AuthContext: Manejo centralizado de autenticación
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// VehicleContext: Estado de vehículos con operaciones CRUD
const VehicleContext = createContext<VehicleContextType | undefined>(undefined);
```

#### 2. **RBAC (Role-Based Access Control)**
```typescript
// Protección de rutas basada en roles
<ProtectedRoute allowedRoles={['administrador']}>
  <Reports />
</ProtectedRoute>

// UI condicional según permisos
{canManageVehicles && <Button>Crear vehículo</Button>}
```

#### 3. **Compound Component Pattern**
```typescript
// Formularios modales reutilizables
<VehicleForm mode="create" onSubmit={createVehicle} />
<VehicleForm mode="edit" vehicle={selected} onSubmit={updateVehicle} />
```

#### 4. **Custom Hooks Pattern**
```typescript
// Hooks para lógica reutilizable
const { user, login, logout } = useAuth();
const { vehicles, isLoading, createVehicle } = useVehicles();
```

## 👥 Roles de Usuario y Control de Acceso

### 🔧 Administrador
- **Rutas completas**: `/fleet`, `/alerts`, `/reports`, `/settings`
- **Permisos**: Crear, editar, eliminar vehículos
- **Dashboard**: Acceso completo a métricas y configuración
- **Redirección**: Automática a `/fleet` después del login

### 👤 Operador
- **Rutas limitadas**: `/alerts`, `/fleet` (solo lectura)
- **Permisos**: Solo visualización de datos
- **Restricciones**: Botones deshabilitados con tooltips informativos
- **Redirección**: Automática a `/alerts` después del login

## 🔐 Credenciales de Prueba

### Administrador
- **Usuario**: `admin`
- **Contraseña**: `admin123`

### Operador
- **Usuario**: `operador`
- **Contraseña**: `op123`

## 🛠️ Stack Tecnológico

### Frontend Core
- **React 18.3+**: Framework principal con hooks y context
- **TypeScript**: Tipado estático para mayor robustez
- **Vite**: Build tool moderno para desarrollo rápido
- **React Router v6**: Navegación con protección de rutas

### UI y Styling
- **TailwindCSS**: Framework CSS utility-first
- **shadcn/ui**: Componentes accesibles y customizables
- **Lucide React**: Iconografía moderna y consistente
- **CSS Variables**: Sistema de tokens para theming

### Herramientas de Desarrollo
- **ESLint**: Linting y mejores prácticas
- **TypeScript Compiler**: Verificación de tipos
- **PostCSS**: Procesamiento de CSS

## 🎨 Sistema de Diseño

### Tema Oscuro Professional
```css
/* Paleta de colores principal */
--primary: 214 84% 56%;        /* Azul corporativo */
--success: 142 76% 46%;        /* Verde para acciones positivas */
--destructive: 0 84% 60%;      /* Rojo para acciones destructivas */
--background: 220 13% 9%;      /* Fondo oscuro principal */
--card: 220 13% 12%;           /* Fondo de tarjetas */
```

### Responsive Breakpoints
- **Mobile**: `< 768px` - Sidebar colapsable, botones touch-friendly
- **Tablet**: `768px - 1024px` - Layout híbrido
- **Desktop**: `> 1024px` - Sidebar completo, múltiples columnas

## 🔄 Flujos de Usuario

### Flujo de Autenticación
1. **Login** → Validación → Redirección según rol
2. **Administrador** → `/fleet` (gestión completa)
3. **Operador** → `/alerts` (monitoreo)
4. **Protección**: Rutas restringidas → 403 Forbidden

### Flujo CRUD de Vehículos
1. **Crear**: Modal → Validación → API call → Toast → Actualizar tabla
2. **Editar**: Selección → Modal precargado → Validación → Guardar
3. **Eliminar**: Confirmación → Verificar restricciones → Eliminar → Toast

## 📱 Características Responsive

### Mobile-First Design
- **Sidebar**: Colapsable con hamburger menu
- **Tablas**: Scroll horizontal con indicadores visuales
- **Formularios**: Campos apilados verticalmente
- **Botones**: Mínimo 44x44px para touch

### Optimizaciones Touch
- **Tooltips**: Adaptados para dispositivos táctiles
- **Modales**: Full-screen en móvil
- **Navegación**: Gestos swipe compatibles

## ♿ Accesibilidad (WCAG 2.1 AA)

### Navegación por Teclado
- **Tab navigation**: Orden lógico en todos los elementos
- **Focus visible**: Indicadores claros de foco
- **Escape key**: Cierre de modales y dropdowns

### Semántica HTML
- **Labels**: Asociados correctamente con inputs
- **Headings**: Jerarquía lógica (h1, h2, h3)
- **ARIA**: Roles y propiedades para lectores de pantalla
- **Live regions**: Anuncios de cambios de estado

### Contraste y Legibilidad
- **Contraste**: Mínimo 4.5:1 para texto normal
- **Tamaños**: Texto mínimo 16px en móvil
- **Estados**: Indicadores visuales claros para disabled/active

## 🔌 Preparación para Backend

### API Interface Preparada
```typescript
// Funciones listas para conectar con REST API
interface VehicleService {
  listVehicles(): Promise<Vehicle[]>;
  createVehicle(data: VehicleFormData): Promise<Vehicle>;
  updateVehicle(id: string, data: VehicleFormData): Promise<Vehicle>;
  deleteVehicle(id: string): Promise<boolean>;
}
```

### Mock Data Current
- **Datos simulados** para desarrollo y testing
- **Estados realistas** incluyendo loading, error, empty
- **Validaciones** preparadas para casos reales

## 🚧 Roadmap y Extensibilidad

### Integraciones Futuras
- **🔗 REST API**: Conexión con backend real
- **🔄 WebSockets**: Actualizaciones en tiempo real
- **📊 Analytics**: Tracking de uso y métricas
- **🔔 Notificaciones**: Push notifications para alertas

### Características Planificadas
- **📱 PWA**: Progressive Web App capabilities
- **🌐 i18n**: Internacionalización multi-idioma
- **🎨 Themes**: Múltiples temas de color
- **📦 Microservices**: Arquitectura distribuida

## 🧪 Testing y Quality

### Validaciones Implementadas
- **Formularios**: Validación en tiempo real con feedback
- **Duplicados**: Prevención de placas duplicadas
- **Restricciones**: Vehículos con viajes activos no eliminables
- **Permisos**: Verificación de roles en cada acción

### Estados de Error Manejados
- **Network errors**: Manejo de fallos de conexión
- **Validation errors**: Feedback específico por campo
- **Permission errors**: Mensajes claros de acceso denegado
- **Loading states**: Indicadores de progreso consistentes

---

Desarrollado para la Fábrica Escuela - FleetGuard360 🚛
