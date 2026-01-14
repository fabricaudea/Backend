import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { BarChart3, Download, Calendar, TrendingUp } from 'lucide-react';
import { Navigation } from '@/components/Navigation';

export const Reports: React.FC = () => {
  return (
    <Navigation>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">Reportes</h1>
            <p className="text-muted-foreground">Análisis y reportes de la gestión de flota</p>
          </div>
          <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
            <Download className="h-4 w-4 mr-2" />
            Exportar Datos
          </Button>
        </div>

        {/* Statistics Overview */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card className="bg-card border-border">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-card-foreground">Eficiencia de Flota</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-card-foreground">87.5%</div>
              <p className="text-xs text-muted-foreground">
                +2.3% desde el mes pasado
              </p>
            </CardContent>
          </Card>
          
          <Card className="bg-card border-border">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-card-foreground">Viajes Completados</CardTitle>
              <BarChart3 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-card-foreground">1,247</div>
              <p className="text-xs text-muted-foreground">
                +15% desde el mes pasado
              </p>
            </CardContent>
          </Card>
          
          <Card className="bg-card border-border">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-card-foreground">Tiempo Promedio</CardTitle>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-card-foreground">45m</div>
              <p className="text-xs text-muted-foreground">
                -5m desde el mes pasado
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Report Categories */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card className="bg-card border-border">
            <CardHeader>
              <CardTitle className="text-card-foreground">Reportes de Vehículos</CardTitle>
              <CardDescription>
                Análisis detallado del rendimiento y uso de vehículos
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
                <BarChart3 className="h-4 w-4 mr-2" />
                Utilización de Vehículos
              </Button>
              <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
                <BarChart3 className="h-4 w-4 mr-2" />
                Mantenimiento y Costos
              </Button>
              <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
                <BarChart3 className="h-4 w-4 mr-2" />
                Consumo de Combustible
              </Button>
            </CardContent>
          </Card>

          <Card className="bg-card border-border">
            <CardHeader>
              <CardTitle className="text-card-foreground">Reportes Operacionales</CardTitle>
              <CardDescription>
                Métricas de operación y eficiencia de la flota
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
                <TrendingUp className="h-4 w-4 mr-2" />
                Eficiencia de Rutas
              </Button>
              <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
                <TrendingUp className="h-4 w-4 mr-2" />
                Tiempos de Respuesta
              </Button>
              <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
                <TrendingUp className="h-4 w-4 mr-2" />
                Alertas y Eventos
              </Button>
            </CardContent>
          </Card>
        </div>

        {/* Placeholder Chart Area */}
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="text-card-foreground">Gráfico de Rendimiento</CardTitle>
            <CardDescription>
              Vista general del rendimiento de la flota en los últimos 30 días
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-64 bg-muted rounded-lg flex items-center justify-center">
              <div className="text-center space-y-2">
                <BarChart3 className="h-12 w-12 text-muted-foreground mx-auto" />
                <p className="text-muted-foreground">Gráfico de rendimiento</p>
                <p className="text-sm text-muted-foreground">
                  Los datos se mostrarán cuando esté conectado a la API
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </Navigation>
  );
};