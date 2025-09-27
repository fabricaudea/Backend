import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { Vehicle, VehicleFormData } from '@/types/vehicle';
import { toast } from '@/hooks/use-toast';
import { apiService } from '@/services/api';

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

export const VehicleProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Cargar vehículos al iniciar
  useEffect(() => {
    console.log('🚀 VehicleContext: Initial load triggered');
    // Clear any potential cached state
    setVehicles([]);
    setError(null);
    refreshVehicles();
  }, []);

  const refreshVehicles = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      console.log('🔄 VehicleContext: Starting to fetch vehicles...');
      const vehiclesData = await apiService.getVehicles();
      console.log('✅ VehicleContext: Received vehicles data:', vehiclesData);
      console.log('📊 VehicleContext: Vehicle count:', vehiclesData?.length || 0);
      
      // Log each vehicle for debugging
      if (vehiclesData && Array.isArray(vehiclesData)) {
        vehiclesData.forEach((vehicle, index) => {
          console.log(`🚗 Vehicle ${index + 1}:`, {
            id: vehicle.id,
            placa: vehicle.placa,
            modelo: vehicle.modelo,
            estado: vehicle.estado
          });
        });
      }
      
      setVehicles(vehiclesData);
      console.log('💾 VehicleContext: State updated with vehicles');
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Error al cargar vehículos';
      setError(errorMessage);
      console.error('❌ VehicleContext: Error loading vehicles:', error);
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

  const updateVehicle = useCallback(async (id: string, data: Partial<VehicleFormData>): Promise<boolean> => {
    setIsLoading(true);
    setError(null);

    try {
      const updatedVehicle = await apiService.updateVehicle(id, data as VehicleFormData);
      
      setVehicles(prev => prev.map(vehicle => 
        vehicle.id === id ? updatedVehicle : vehicle
      ));

      toast({
        title: "Éxito",
        description: "Vehículo actualizado con éxito",
        variant: "default",
      });

      setIsLoading(false);
      return true;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Error al actualizar vehículo';
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

      await apiService.deleteVehicle(id);
      setVehicles(prev => prev.filter(vehicle => vehicle.id !== id));

      toast({
        title: "Éxito",
        description: "Vehículo eliminado",
        variant: "default",
      });

      setIsLoading(false);
      return true;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Error al eliminar vehículo';
      setError(errorMessage);
      toast({
        title: "Error",
        description: errorMessage,
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