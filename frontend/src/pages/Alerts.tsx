import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { AlertTriangle, Clock, RefreshCw } from 'lucide-react';
import { Navigation } from '@/components/Navigation';

// Mock data for alerts
const mockAlerts = [
  {
    id: '1',
    title: 'Vehículo ABC-123 requiere mantenimiento preventivo',
    description: 'El vehículo ha superado los 10,000 km desde el último mantenimiento.',
    severity: 'warning' as const,
    timestamp: '2024-01-15 10:30',
    status: 'pending' as const
  },
  {
    id: '2',
    title: 'Retraso en ruta - Vehículo XYZ-789',
    description: 'El vehículo lleva 25 minutos de retraso en su ruta programada.',
    severity: 'high' as const,
    timestamp: '2024-01-15 09:45',
    status: 'active' as const
  },
  {
    id: '3',
    title: 'Combustible bajo - Vehículo DEF-456',
    description: 'Nivel de combustible por debajo del 15%. Se recomienda reabastecimiento.',
    severity: 'medium' as const,
    timestamp: '2024-01-15 08:20',
    status: 'resolved' as const
  }
];

const getSeverityColor = (severity: string) => {
  switch (severity) {
    case 'high':
      return 'bg-destructive text-destructive-foreground';
    case 'warning':
      return 'bg-orange-500 text-white';
    case 'medium':
      return 'bg-yellow-500 text-white';
    default:
      return 'bg-muted text-muted-foreground';
  }
};

const getSeverityLabel = (severity: string) => {
  switch (severity) {
    case 'high':
      return 'Alta';
    case 'warning':
      return 'Advertencia';
    case 'medium':
      return 'Media';
    default:
      return severity;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'active':
      return 'bg-destructive text-destructive-foreground';
    case 'pending':
      return 'bg-yellow-500 text-white';
    case 'resolved':
      return 'bg-green-500 text-white';
    default:
      return 'bg-muted text-muted-foreground';
  }
};

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'active':
      return 'Activa';
    case 'pending':
      return 'Pendiente';
    case 'resolved':
      return 'Resuelta';
    default:
      return status;
  }
};

export const Alerts: React.FC = () => {
  return (
    <Navigation>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">Panel de Alertas</h1>
            <p className="text-muted-foreground">Monitorea las alertas y eventos de la flota</p>
          </div>
          <Button variant="outline" size="sm" className="border-border text-card-foreground hover:bg-muted">
            <RefreshCw className="h-4 w-4 mr-2" />
            Actualizar
          </Button>
        </div>

        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card className="bg-card border-border">
            <CardContent className="p-4">
              <div className="flex items-center space-x-2">
                <div className="w-2 h-2 bg-destructive rounded-full"></div>
                <div>
                  <p className="text-sm font-medium text-card-foreground">Alertas Activas</p>
                  <p className="text-2xl font-bold text-card-foreground">1</p>
                </div>
              </div>
            </CardContent>
          </Card>
          
          <Card className="bg-card border-border">
            <CardContent className="p-4">
              <div className="flex items-center space-x-2">
                <div className="w-2 h-2 bg-warning rounded-full"></div>
                <div>
                  <p className="text-sm font-medium text-card-foreground">Pendientes</p>
                  <p className="text-2xl font-bold text-card-foreground">1</p>
                </div>
              </div>
            </CardContent>
          </Card>
          
          <Card className="bg-card border-border">
            <CardContent className="p-4">
              <div className="flex items-center space-x-2">
                <div className="w-2 h-2 bg-success rounded-full"></div>
                <div>
                  <p className="text-sm font-medium text-card-foreground">Resueltas</p>
                  <p className="text-2xl font-bold text-card-foreground">1</p>
                </div>
              </div>
            </CardContent>
          </Card>
          
          <Card className="bg-card border-border">
            <CardContent className="p-4">
              <div className="flex items-center space-x-2">
                <AlertTriangle className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-card-foreground">Total Hoy</p>
                  <p className="text-2xl font-bold text-card-foreground">3</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Alerts List */}
        <div className="space-y-4">
          <h2 className="text-lg font-semibold text-foreground">Alertas Recientes</h2>
          
          {mockAlerts.map((alert) => (
            <Card key={alert.id} className="bg-card border-border border-l-4 border-l-warning">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-base font-medium text-card-foreground">
                    {alert.title}
                  </CardTitle>
                  <div className="flex items-center space-x-2">
                    <Badge className={getSeverityColor(alert.severity)}>
                      {getSeverityLabel(alert.severity)}
                    </Badge>
                    <Badge className={getStatusColor(alert.status)}>
                      {getStatusLabel(alert.status)}
                    </Badge>
                  </div>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <CardDescription className="text-muted-foreground mb-3">
                  {alert.description}
                </CardDescription>
                
                <div className="flex items-center space-x-4 text-sm text-muted-foreground">
                  <div className="flex items-center space-x-1">
                    <Clock className="h-3 w-3" />
                    <span>{alert.timestamp}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </Navigation>
  );
};