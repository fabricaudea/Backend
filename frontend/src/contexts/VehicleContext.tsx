import React, { createContext, useContext, useState, useCallback } from 'react';
import { Vehicle, VehicleFormData } from '@/types/vehicle';
import { toast } from '@/hooks/use-toast';

interface VehicleContextType {
  vehicles: Vehicle[];
  isLoading: boolean;
  error: string | null;
  createVehicle: (data: VehicleFormData) => Promise<boolean>;
  updateVehicle: (id: string, data: Partial<VehicleFormData>) => Promise<boolean>;
  deleteVehicle: (id: string) => Promise<boolean>;
  getVehicle: (id: string) => Vehicle | undefined;
  refreshVehicles: () => Promise<void>;
}

const VehicleContext = createContext<VehicleContextType | undefined>(undefined);

// Mock data
const mockVehicles: Vehicle[] = [
  {
    id: '1',
    placa: 'ABC-123',
    modelo: 'Mercedes Sprinter 2023',
    capacidad: 12,
    estado: 'activo',
    fechaCreacion: '2024-01-15',
    fechaActualizacion: '2024-01-15',
    viajesActivos: 0
  },
  {
    id: '2',
    placa: 'DEF-456',
    modelo: 'Ford Transit 2022',
    capacidad: 8,
    estado: 'mantenimiento',
    fechaCreacion: '2024-02-20',
    fechaActualizacion: '2024-03-10',
    viajesActivos: 0
  },
  {
    id: '3',
    placa: 'GHI-789',
    modelo: 'Iveco Daily 2023',
    capacidad: 15,
    estado: 'activo',
    fechaCreacion: '2024-03-05',
    fechaActualizacion: '2024-03-05',
    viajesActivos: 2
  }
];

export const VehicleProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [vehicles, setVehicles] = useState<Vehicle[]>(mockVehicles);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const generateId = () => Math.random().toString(36).substr(2, 9);

  const refreshVehicles = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 500));
    
    setIsLoading(false);
  }, []);

  const createVehicle = useCallback(async (data: VehicleFormData): Promise<boolean> => {
    setIsLoading(true);
    setError(null);

    try {
      // Check for duplicate placa
      const existingVehicle = vehicles.find(v => v.placa.toLowerCase() === data.placa.toLowerCase());
      if (existingVehicle) {
        toast({
          title: "Error",
          description: "Ya existe un vehículo con esta placa",
          variant: "destructive",
        });
        setIsLoading(false);
        return false;
      }

      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      const newVehicle: Vehicle = {
        id: generateId(),
        ...data,
        placa: data.placa.toUpperCase(),
        fechaCreacion: new Date().toISOString().split('T')[0],
        fechaActualizacion: new Date().toISOString().split('T')[0],
        viajesActivos: 0
      };

      setVehicles(prev => [newVehicle, ...prev]);
      
      toast({
        title: "Éxito",
        description: "Vehículo creado con éxito",
        variant: "default",
      });

      setIsLoading(false);
      return true;
    } catch (error) {
      setError('Error al crear vehículo');
      toast({
        title: "Error",
        description: "Error al registrar vehículo",
        variant: "destructive",
      });
      setIsLoading(false);
      return false;
    }
  }, [vehicles]);

  const updateVehicle = useCallback(async (id: string, data: Partial<VehicleFormData>): Promise<boolean> => {
    setIsLoading(true);
    setError(null);

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      setVehicles(prev => prev.map(vehicle => 
        vehicle.id === id 
          ? { 
              ...vehicle, 
              ...data,
              fechaActualizacion: new Date().toISOString().split('T')[0]
            }
          : vehicle
      ));

      toast({
        title: "Éxito",
        description: "Vehículo actualizado con éxito",
        variant: "default",
      });

      setIsLoading(false);
      return true;
    } catch (error) {
      setError('Error al actualizar vehículo');
      toast({
        title: "Error",
        description: "Error al actualizar vehículo",
        variant: "destructive",
      });
      setIsLoading(false);
      return false;
    }
  }, []);

  const deleteVehicle = useCallback(async (id: string): Promise<boolean> => {
    setIsLoading(true);
    setError(null);

    try {
      const vehicle = vehicles.find(v => v.id === id);
      
      if (vehicle?.viajesActivos && vehicle.viajesActivos > 0) {
        toast({
          title: "Error",
          description: "No se puede eliminar un vehículo con viajes activos",
          variant: "destructive",
        });
        setIsLoading(false);
        return false;
      }

      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      setVehicles(prev => prev.filter(vehicle => vehicle.id !== id));

      toast({
        title: "Éxito",
        description: "Vehículo eliminado",
        variant: "default",
      });

      setIsLoading(false);
      return true;
    } catch (error) {
      setError('Error al eliminar vehículo');
      toast({
        title: "Error",
        description: "No se pudo eliminar",
        variant: "destructive",
      });
      setIsLoading(false);
      return false;
    }
  }, [vehicles]);

  const getVehicle = useCallback((id: string): Vehicle | undefined => {
    return vehicles.find(vehicle => vehicle.id === id);
  }, [vehicles]);

  return (
    <VehicleContext.Provider value={{
      vehicles,
      isLoading,
      error,
      createVehicle,
      updateVehicle,
      deleteVehicle,
      getVehicle,
      refreshVehicles
    }}>
      {children}
    </VehicleContext.Provider>
  );
};

export const useVehicles = () => {
  const context = useContext(VehicleContext);
  if (context === undefined) {
    throw new Error('useVehicles must be used within a VehicleProvider');
  }
  return context;
};