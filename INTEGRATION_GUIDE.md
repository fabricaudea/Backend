# Integración Frontend-Backend FleetGuard360

## Resumen de la Integración

He adaptado completamente el backend Spring Boot para que sea compatible con el frontend React. Aquí están los cambios realizados y las instrucciones para conectar ambos sistemas.

## Cambios Realizados en el Backend

### 1. Nuevos DTOs Compatibles con Frontend

- **VehicleResponseFrontend.java**: DTO que mapea los campos del backend a los nombres esperados por el frontend
  - `id` (String) ← `id` (Long)
  - `placa` ← `licensePlate`
  - `modelo` ← `model`
  - `capacidad` ← `capacity`
  - `estado` ← `status` (mapeado a strings: "activo", "inactivo", "mantenimiento")
  - `fechaCreacion` ← `createdAt` (formato string YYYY-MM-DD)
  - `fechaActualizacion` ← `updatedAt` (formato string YYYY-MM-DD)
  - `viajesActivos` (por ahora siempre 0)

- **VehicleFormRequest.java**: DTO para requests de creación/actualización desde frontend
- **UserResponseFrontend.java**: DTO para respuestas de usuario compatible con frontend

### 2. Nuevos Controladores Específicos para Frontend

- **VehicleFrontendController.java** (`/api/frontend/vehicles`)
  - GET `/api/frontend/vehicles` - Lista todos los vehículos
  - GET `/api/frontend/vehicles/{id}` - Obtiene vehículo por ID
  - POST `/api/frontend/vehicles` - Crea nuevo vehículo
  - PUT `/api/frontend/vehicles/{id}` - Actualiza vehículo
  - DELETE `/api/frontend/vehicles/{id}` - Elimina vehículo

- **AuthFrontendController.java** (`/api/frontend/auth`)
  - POST `/api/frontend/auth/login` - Login compatible con frontend
  - POST `/api/frontend/auth/logout` - Logout
  - GET `/api/frontend/auth/me` - Verificar usuario actual

### 3. Configuración de CORS y Seguridad

- Configurada clase `SecurityConfig` con soporte CORS completo
- Endpoints del frontend permitidos en la configuración de seguridad
- CORS configurado para permitir `localhost:5173` (Vite dev server)

### 4. Datos de Prueba

- Usuario administrador: `admin` / `admin123`
- Usuario operador: `operador` / `op123`
- 3 vehículos de prueba que coinciden con los datos mock del frontend

## Instrucciones para el Frontend

### 1. Crear Servicio de API

Crea un archivo `src/services/api.ts`:

```typescript
const API_BASE_URL = 'http://localhost:8080/api/frontend';

class ApiService {
  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const defaultOptions: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Importante para las sesiones
      ...options,
    };

    const response = await fetch(url, defaultOptions);
    
    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Error desconocido' }));
      throw new Error(error.message || `HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  // Autenticación
  async login(credentials: { username: string; password: string }) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  }

  async logout() {
    return this.request('/auth/logout', {
      method: 'POST',
    });
  }

  async getCurrentUser() {
    return this.request('/auth/me');
  }

  // Vehículos
  async getVehicles() {
    return this.request('/vehicles');
  }

  async getVehicle(id: string) {
    return this.request(`/vehicles/${id}`);
  }

  async createVehicle(vehicle: { placa: string; modelo: string; capacidad: number; estado: string }) {
    return this.request('/vehicles', {
      method: 'POST',
      body: JSON.stringify(vehicle),
    });
  }

  async updateVehicle(id: string, vehicle: { placa: string; modelo: string; capacidad: number; estado: string }) {
    return this.request(`/vehicles/${id}`, {
      method: 'PUT',
      body: JSON.stringify(vehicle),
    });
  }

  async deleteVehicle(id: string) {
    return this.request(`/vehicles/${id}`, {
      method: 'DELETE',
    });
  }
}

export const apiService = new ApiService();
```

### 2. Actualizar AuthContext

Reemplaza las llamadas mock en `AuthContext.tsx`:

```typescript
const login = async (credentials: LoginCredentials): Promise<boolean> => {
  setIsLoading(true);
  
  try {
    const user = await apiService.login(credentials);
    setUser(user);
    localStorage.setItem('fleetguard_user', JSON.stringify(user));
    setIsLoading(false);
    return true;
  } catch (error) {
    setIsLoading(false);
    return false;
  }
};

const logout = async () => {
  try {
    await apiService.logout();
  } catch (error) {
    console.error('Error during logout:', error);
  }
  setUser(null);
  localStorage.removeItem('fleetguard_user');
};
```

### 3. Actualizar VehicleContext

Reemplaza las operaciones mock en `VehicleContext.tsx`:

```typescript
const refreshVehicles = useCallback(async () => {
  setIsLoading(true);
  setError(null);
  
  try {
    const vehiclesData = await apiService.getVehicles();
    setVehicles(vehiclesData);
  } catch (error) {
    setError('Error al cargar vehículos');
  }
  
  setIsLoading(false);
}, []);

const createVehicle = useCallback(async (data: VehicleFormData): Promise<boolean> => {
  setIsLoading(true);
  setError(null);

  try {
    const newVehicle = await apiService.createVehicle(data);
    setVehicles(prev => [newVehicle, ...prev]);
    
    toast({
      title: "Éxito",
      description: "Vehículo creado con éxito",
      variant: "default",
    });

    setIsLoading(false);
    return true;
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : 'Error al crear vehículo';
    setError(errorMessage);
    toast({
      title: "Error",
      description: errorMessage,
      variant: "destructive",
    });
    setIsLoading(false);
    return false;
  }
}, []);

// Similar para updateVehicle y deleteVehicle
```

### 4. Inicializar Datos al Cargar

En `VehicleContext.tsx`, agrega:

```typescript
useEffect(() => {
  refreshVehicles();
}, [refreshVehicles]);
```

## Cómo Probar la Integración

### 1. Iniciar el Backend
```bash
cd backend
./mvnw spring-boot:run
```
El backend estará disponible en `http://localhost:8080`

### 2. Verificar Base de Datos
Asegúrate de tener MySQL corriendo y la base de datos configurada según `application.properties`

### 3. Iniciar el Frontend
```bash
cd frontend
npm run dev
```
El frontend estará disponible en `http://localhost:5173`

### 4. Probar la Autenticación
- Usuario: `admin` / Contraseña: `admin123` (rol administrador)
- Usuario: `operador` / Contraseña: `op123` (rol operador)

### 5. Verificar Endpoints
Puedes probar los endpoints directamente:
- GET `http://localhost:8080/api/frontend/vehicles`
- POST `http://localhost:8080/api/frontend/auth/login`

## Mapeo de Estados

El backend maneja estos estados internos que se mapean al frontend:

| Backend (VehicleStatus) | Frontend (estado) |
|------------------------|-------------------|
| AVAILABLE, IN_USE      | "activo"          |
| MAINTENANCE            | "mantenimiento"   |
| OUT_OF_SERVICE, INACTIVE | "inactivo"      |

## Notas Importantes

1. **Sesiones**: El backend usa sesiones HTTP, no JWT. Asegúrate de incluir `credentials: 'include'` en las peticiones fetch.

2. **CORS**: El backend está configurado para permitir requests desde `localhost:5173` (Vite default).

3. **Roles**: Los roles del backend (ADMIN, USER) se mapean automáticamente a ("administrador", "operador") para el frontend.

4. **Validación**: El backend incluye validación completa de datos, por lo que errores serán retornados en formato JSON.

5. **Fechas**: Las fechas se convierten automáticamente del formato LocalDateTime del backend al formato string esperado por el frontend.

Con esta integración, el frontend debería funcionar perfectamente con el backend real en lugar de los datos mock.