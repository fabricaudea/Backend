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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Gestión de Flota</h1>
          <p className="text-muted-foreground">Administra los vehículos de tu flota</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={refreshVehicles}
            disabled={isLoading}
            size="sm"
          >
            <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
          </Button>
          
          {canManageVehicles ? (
            <Button 
              onClick={() => setCreateModalOpen(true)}
              className="bg-success text-success-foreground hover:bg-success/90"
            >
              <Plus className="h-4 w-4 mr-2" />
              Dar de alta vehículo
            </Button>
          ) : (
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <div>
                    <Button 
                      disabled
                      className="bg-success text-success-foreground hover:bg-success/90 opacity-50 cursor-not-allowed"
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      Dar de alta vehículo
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
          <div className="mx-auto w-24 h-24 bg-muted rounded-full flex items-center justify-center mb-4">
            <Plus className="h-12 w-12 text-muted-foreground" />
          </div>
          <h3 className="text-lg font-medium text-foreground mb-2">No hay vehículos registrados</h3>
          <p className="text-muted-foreground mb-4">Comienza agregando tu primer vehículo a la flota</p>
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
        <div className="bg-card border border-border rounded-lg">
          <div className="table-container scrollbar-thin">
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-muted/50">
                  <TableHead className="text-card-foreground">Placa</TableHead>
                  <TableHead className="text-card-foreground">Modelo</TableHead>
                  <TableHead className="text-card-foreground">Capacidad</TableHead>
                  <TableHead className="text-card-foreground">Estado</TableHead>
                  <TableHead className="text-card-foreground">Viajes Activos</TableHead>
                  <TableHead className="text-right text-card-foreground">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {vehicles.map((vehicle) => (
                  <TableRow key={vehicle.id} className="hover:bg-muted/30">
                    <TableCell className="font-medium text-card-foreground">{vehicle.placa}</TableCell>
                    <TableCell className="text-card-foreground">{vehicle.modelo}</TableCell>
                    <TableCell className="text-card-foreground">{vehicle.capacidad} personas</TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(vehicle.estado)}>
                        {getStatusLabel(vehicle.estado)}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-card-foreground">{vehicle.viajesActivos || 0}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        {canManageVehicles ? (
                          <>
                            <Button
                              size="sm"
                              onClick={() => handleEdit(vehicle)}
                              disabled={isLoading}
                              className="bg-primary hover:bg-primary-hover text-primary-foreground min-w-[44px] min-h-[44px] md:min-w-auto md:min-h-auto"
                            >
                              <Edit className="h-4 w-4 md:mr-2" />
                              <span className="hidden md:inline">Editar</span>
                            </Button>
                            <Button
                              size="sm"
                              onClick={() => handleDelete(vehicle)}
                              disabled={isLoading}
                              className="bg-destructive hover:bg-destructive-hover text-destructive-foreground min-w-[44px] min-h-[44px] md:min-w-auto md:min-h-auto"
                            >
                              <Trash2 className="h-4 w-4 md:mr-2" />
                              <span className="hidden md:inline">Dar de baja</span>
                            </Button>
                          </>
                        ) : (
                          <TooltipProvider>
                            <div className="flex gap-2">
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <div>
                                    <Button
                                      size="sm"
                                      disabled
                                      className="text-muted-foreground border-muted cursor-not-allowed opacity-50 min-w-[44px] min-h-[44px] md:min-w-auto md:min-h-auto"
                                    >
                                      <Edit className="h-4 w-4 md:mr-2" />
                                      <span className="hidden md:inline">Editar</span>
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
                                      className="text-muted-foreground border-muted cursor-not-allowed opacity-50 min-w-[44px] min-h-[44px] md:min-w-auto md:min-h-auto"
                                    >
                                      <Trash2 className="h-4 w-4 md:mr-2" />
                                      <span className="hidden md:inline">Dar de baja</span>
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