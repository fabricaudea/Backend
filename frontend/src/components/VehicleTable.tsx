import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { Edit, Trash2, Plus, RefreshCw } from 'lucide-react';
import { Vehicle } from '@/types/vehicle';
import { VehicleForm } from './VehicleForm';
import { ConfirmDialog } from './ConfirmDialog';
import { useVehicles } from '@/contexts/VehicleContext';
import { useAuth } from '@/contexts/AuthContext';

const getStatusColor = (estado: Vehicle['estado']) => {
  switch (estado) {
    case 'activo':
      return 'bg-status-active text-white';
    case 'inactivo':
      return 'bg-status-inactive text-white';
    case 'mantenimiento':
      return 'bg-status-maintenance text-white';
    default:
      return 'bg-muted text-muted-foreground';
  }
};

const getStatusLabel = (estado: Vehicle['estado']) => {
  switch (estado) {
    case 'activo':
      return 'Activo';
    case 'inactivo':
      return 'Inactivo';
    case 'mantenimiento':
      return 'Mantenimiento';
    default:
      return estado;
  }
};

export const VehicleTable: React.FC = () => {
  const { vehicles, isLoading, createVehicle, updateVehicle, deleteVehicle, refreshVehicles } = useVehicles();
  const { user } = useAuth();
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);

  const canManageVehicles = user?.role === 'administrador';

  const handleEdit = (vehicle: Vehicle) => {
    setSelectedVehicle(vehicle);
    setEditModalOpen(true);
  };

  const handleDelete = (vehicle: Vehicle) => {
    setSelectedVehicle(vehicle);
    setDeleteModalOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (selectedVehicle) {
      const success = await deleteVehicle(selectedVehicle.id);
      if (success) {
        setDeleteModalOpen(false);
        setSelectedVehicle(null);
      }
    }
  };

  const handleEditSubmit = async (data: any) => {
    if (selectedVehicle) {
      const success = await updateVehicle(selectedVehicle.id, data);
      if (success) {
        setSelectedVehicle(null);
      }
      return success;
    }
    return false;
  };

  if (isLoading && vehicles.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <RefreshCw className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2 text-muted-foreground">Cargando vehículos...</span>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-foreground">Gestión de Flota</h1>
          <p className="text-sm sm:text-base text-muted-foreground">Administra los vehículos de tu flota</p>
        </div>
        <div className="flex flex-col sm:flex-row gap-2 sm:gap-2">
          <Button
            variant="outline"
            onClick={refreshVehicles}
            disabled={isLoading}
            size="sm"
            className="w-full sm:w-auto"
          >
            <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''} sm:mr-2`} />
            <span className="hidden sm:inline">Actualizar</span>
          </Button>
          
          {canManageVehicles ? (
            <Button 
              onClick={() => setCreateModalOpen(true)}
              className="w-full sm:w-auto bg-success text-success-foreground hover:bg-success/90"
            >
              <Plus className="h-4 w-4 mr-2" />
              <span className="hidden sm:inline">Dar de alta vehículo</span>
              <span className="sm:hidden">Agregar</span>
            </Button>
          ) : (
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <div className="w-full sm:w-auto">
                    <Button 
                      disabled
                      className="w-full bg-success text-success-foreground hover:bg-success/90 opacity-50 cursor-not-allowed"
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      <span className="hidden sm:inline">Dar de alta vehículo</span>
                      <span className="sm:hidden">Agregar</span>
                    </Button>
                  </div>
                </TooltipTrigger>
                <TooltipContent>
                  <p>Permiso requerido - Solo administradores</p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          )}
        </div>
      </div>

      {/* Table */}
      {vehicles.length === 0 ? (
        <div className="text-center py-12">
          <div className="mx-auto w-16 h-16 sm:w-24 sm:h-24 bg-muted rounded-full flex items-center justify-center mb-4">
            <Plus className="h-8 w-8 sm:h-12 sm:w-12 text-muted-foreground" />
          </div>
          <h3 className="text-base sm:text-lg font-medium text-foreground mb-2">No hay vehículos registrados</h3>
          <p className="text-sm sm:text-base text-muted-foreground mb-4">Comienza agregando tu primer vehículo a la flota</p>
          {canManageVehicles && (
            <Button 
              onClick={() => setCreateModalOpen(true)}
              className="bg-success text-success-foreground hover:bg-success/90"
            >
              <Plus className="h-4 w-4 mr-2" />
              Agregar vehículo
            </Button>
          )}
        </div>
      ) : (
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="overflow-x-auto">
            <Table className="min-w-full">
              <TableHeader>
                <TableRow className="hover:bg-muted/50 border-b border-border">
                  <TableHead className="text-card-foreground font-medium px-4 py-3 text-left whitespace-nowrap">Placa</TableHead>
                  <TableHead className="text-card-foreground font-medium px-4 py-3 text-left whitespace-nowrap">Modelo</TableHead>
                  <TableHead className="text-card-foreground font-medium px-4 py-3 text-left whitespace-nowrap hidden sm:table-cell">Capacidad</TableHead>
                  <TableHead className="text-card-foreground font-medium px-4 py-3 text-left whitespace-nowrap">Estado</TableHead>
                  <TableHead className="text-card-foreground font-medium px-4 py-3 text-left whitespace-nowrap hidden lg:table-cell">Viajes Activos</TableHead>
                  <TableHead className="text-card-foreground font-medium px-4 py-3 text-right whitespace-nowrap">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {vehicles.map((vehicle) => (
                  <TableRow key={vehicle.id} className="hover:bg-muted/30 border-b border-border last:border-b-0">
                    <TableCell className="font-medium text-card-foreground px-4 py-4 whitespace-nowrap">
                      {vehicle.placa}
                    </TableCell>
                    <TableCell className="text-card-foreground px-4 py-4">
                      <div className="max-w-[200px] truncate" title={vehicle.modelo}>
                        {vehicle.modelo}
                      </div>
                    </TableCell>
                    <TableCell className="text-card-foreground px-4 py-4 whitespace-nowrap hidden sm:table-cell">
                      {vehicle.capacidad} personas
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <Badge className={`${getStatusColor(vehicle.estado)} whitespace-nowrap`}>
                        {getStatusLabel(vehicle.estado)}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-card-foreground px-4 py-4 whitespace-nowrap hidden lg:table-cell">
                      {vehicle.viajesActivos || 0}
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="flex items-center justify-end gap-1 sm:gap-2">
                        {canManageVehicles ? (
                          <>
                            <Button
                              size="sm"
                              onClick={() => handleEdit(vehicle)}
                              disabled={isLoading}
                              className="bg-primary hover:bg-primary-hover text-primary-foreground min-w-[40px] h-[40px] sm:min-w-auto sm:h-auto p-2 sm:px-3 sm:py-2"
                            >
                              <Edit className="h-4 w-4 sm:mr-2" />
                              <span className="hidden sm:inline">Editar</span>
                            </Button>
                            <Button
                              size="sm"
                              onClick={() => handleDelete(vehicle)}
                              disabled={isLoading}
                              className="bg-destructive hover:bg-destructive-hover text-destructive-foreground min-w-[40px] h-[40px] sm:min-w-auto sm:h-auto p-2 sm:px-3 sm:py-2"
                            >
                              <Trash2 className="h-4 w-4 sm:mr-2" />
                              <span className="hidden sm:inline">Dar de baja</span>
                            </Button>
                          </>
                        ) : (
                          <TooltipProvider>
                            <div className="flex gap-1 sm:gap-2">
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <div>
                                    <Button
                                      size="sm"
                                      disabled
                                      className="text-muted-foreground border-muted cursor-not-allowed opacity-50 min-w-[40px] h-[40px] sm:min-w-auto sm:h-auto p-2 sm:px-3 sm:py-2"
                                    >
                                      <Edit className="h-4 w-4 sm:mr-2" />
                                      <span className="hidden sm:inline">Editar</span>
                                    </Button>
                                  </div>
                                </TooltipTrigger>
                                <TooltipContent>
                                  <p>Permiso requerido - Solo administradores</p>
                                </TooltipContent>
                              </Tooltip>
                              
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <div>
                                    <Button
                                      size="sm"
                                      disabled
                                      className="text-muted-foreground border-muted cursor-not-allowed opacity-50 min-w-[40px] h-[40px] sm:min-w-auto sm:h-auto p-2 sm:px-3 sm:py-2"
                                    >
                                      <Trash2 className="h-4 w-4 sm:mr-2" />
                                      <span className="hidden sm:inline">Dar de baja</span>
                                    </Button>
                                  </div>
                                </TooltipTrigger>
                                <TooltipContent>
                                  <p>Permiso requerido - Solo administradores</p>
                                </TooltipContent>
                              </Tooltip>
                            </div>
                          </TooltipProvider>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>
      )}

      {/* Modals - Only render if user can manage vehicles */}
      {canManageVehicles && (
        <>
          <VehicleForm
            open={createModalOpen}
            onOpenChange={setCreateModalOpen}
            onSubmit={createVehicle}
            mode="create"
            isLoading={isLoading}
          />

          <VehicleForm
            open={editModalOpen}
            onOpenChange={setEditModalOpen}
            onSubmit={handleEditSubmit}
            vehicle={selectedVehicle || undefined}
            mode="edit"
            isLoading={isLoading}
          />

          <ConfirmDialog
            open={deleteModalOpen}
            onOpenChange={setDeleteModalOpen}
            title="¿Eliminar vehículo?"
            description={
              selectedVehicle?.viajesActivos && selectedVehicle.viajesActivos > 0
                ? "No se puede eliminar un vehículo con viajes activos."
                : `¿Seguro que deseas eliminar el vehículo ${selectedVehicle?.placa}? Esta acción no se puede deshacer.`
            }
            confirmText="Eliminar"
            onConfirm={handleConfirmDelete}
            isLoading={isLoading}
            variant="destructive"
          />
        </>
      )}
    </div>
  );
};