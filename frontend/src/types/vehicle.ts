export interface Vehicle {
  id: string;
  placa: string;
  modelo: string;
  capacidad: number;
  estado: 'activo' | 'inactivo' | 'mantenimiento';
  fechaCreacion: string;
  fechaActualizacion: string;
  viajesActivos?: number;
}

export interface VehicleFormData {
  placa: string;
  modelo: string;
  capacidad: number;
  estado: 'activo' | 'inactivo' | 'mantenimiento';
}

export interface User {
  id: string;
  username: string;
  role: 'administrador' | 'operador';
  name: string;
}

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthContextType {
  user: User | null;
  login: (credentials: LoginCredentials) => Promise<boolean>;
  logout: () => void;
  isLoading: boolean;
}