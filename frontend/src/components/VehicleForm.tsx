import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Vehicle, VehicleFormData } from '@/types/vehicle';

interface VehicleFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: VehicleFormData) => Promise<boolean>;
  vehicle?: Vehicle;
  isLoading?: boolean;
  mode: 'create' | 'edit';
}

export const VehicleForm: React.FC<VehicleFormProps> = ({
  open,
  onOpenChange,
  onSubmit,
  vehicle,
  isLoading = false,
  mode
}) => {
  const [formData, setFormData] = useState<VehicleFormData>({
    placa: '',
    modelo: '',
    capacidad: 1,
    estado: 'activo'
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  useEffect(() => {
    if (vehicle && mode === 'edit') {
      setFormData({
        placa: vehicle.placa,
        modelo: vehicle.modelo,
        capacidad: vehicle.capacidad,
        estado: vehicle.estado
      });
    } else if (mode === 'create') {
      setFormData({
        placa: '',
        modelo: '',
        capacidad: 1,
        estado: 'activo'
      });
    }
    setErrors({});
  }, [vehicle, mode, open]);

  const validateForm = (): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (!formData.placa.trim()) {
      newErrors.placa = 'La placa es requerida';
    } else if (!/^[A-Z]{3}-\d{3}$/.test(formData.placa.toUpperCase())) {
      newErrors.placa = 'Formato de placa inválido (ej: ABC-123)';
    }

    if (!formData.modelo.trim()) {
      newErrors.modelo = 'El modelo es requerido';
    }

    if (formData.capacidad < 1 || formData.capacidad > 50) {
      newErrors.capacidad = 'La capacidad debe estar entre 1 y 50';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    const success = await onSubmit(formData);
    if (success) {
      onOpenChange(false);
    }
  };

  const handleInputChange = (field: keyof VehicleFormData, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  const handleCapacidadChange = (value: string) => {
    const numValue = parseInt(value) || 1;
    handleInputChange('capacidad', numValue);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md bg-card border-border">
        <DialogHeader>
          <DialogTitle className="text-card-foreground">
            {mode === 'create' ? 'Dar de alta vehículo' : 'Editar vehículo'}
          </DialogTitle>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="placa" className="text-card-foreground">Placa *</Label>
            <Input
              id="placa"
              value={formData.placa}
              onChange={(e) => handleInputChange('placa', e.target.value.toUpperCase())}
              placeholder="ABC-123"
              disabled={isLoading || mode === 'edit'}
              className={`bg-input border-border text-card-foreground ${errors.placa ? 'border-destructive' : ''}`}
              aria-describedby={errors.placa ? 'placa-error' : undefined}
            />
            {errors.placa && (
              <p id="placa-error" className="text-sm text-destructive" role="alert">
                {errors.placa}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="modelo" className="text-card-foreground">Modelo *</Label>
            <Input
              id="modelo"
              value={formData.modelo}
              onChange={(e) => handleInputChange('modelo', e.target.value)}
              placeholder="Mercedes Sprinter 2023"
              disabled={isLoading}
              className={`bg-input border-border text-card-foreground ${errors.modelo ? 'border-destructive' : ''}`}
              aria-describedby={errors.modelo ? 'modelo-error' : undefined}
            />
            {errors.modelo && (
              <p id="modelo-error" className="text-sm text-destructive" role="alert">
                {errors.modelo}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="capacidad" className="text-card-foreground">Capacidad *</Label>
            <Input
              id="capacidad"
              type="number"
              min="1"
              max="50"
              value={formData.capacidad}
              onChange={(e) => handleCapacidadChange(e.target.value)}
              disabled={isLoading}
              className={`bg-input border-border text-card-foreground ${errors.capacidad ? 'border-destructive' : ''}`}
              aria-describedby={errors.capacidad ? 'capacidad-error' : undefined}
            />
            {errors.capacidad && (
              <p id="capacidad-error" className="text-sm text-destructive" role="alert">
                {errors.capacidad}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="estado" className="text-card-foreground">Estado *</Label>
            <Select
              value={formData.estado}
              onValueChange={(value) => handleInputChange('estado', value as 'activo' | 'inactivo' | 'mantenimiento')}
              disabled={isLoading}
            >
              <SelectTrigger className="bg-input border-border text-card-foreground">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-popover border-border">
                <SelectItem value="activo">Activo</SelectItem>
                <SelectItem value="inactivo">Inactivo</SelectItem>
                <SelectItem value="mantenimiento">Mantenimiento</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <DialogFooter className="gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
              className="border-border text-card-foreground hover:bg-muted"
            >
              Cancelar
            </Button>
            <Button
              type="submit"
              disabled={isLoading}
              className="bg-success text-success-foreground hover:bg-success/90"
            >
              {isLoading 
                ? 'Procesando...' 
                : mode === 'create' 
                  ? 'Registrar vehículo' 
                  : 'Guardar cambios'
              }
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};