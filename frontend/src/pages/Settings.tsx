import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Settings as SettingsIcon, Save, User, Bell, Shield, Database } from 'lucide-react';
import { Navigation } from '@/components/Navigation';

export const Settings: React.FC = () => {
  return (
    <Navigation>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">Configuración</h1>
            <p className="text-muted-foreground">Administra la configuración del sistema</p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Settings Menu */}
          <div className="space-y-2">
            <h2 className="text-lg font-semibold text-foreground mb-4">Categorías</h2>
            
            <Button variant="secondary" className="w-full justify-start bg-secondary text-secondary-foreground hover:bg-secondary/80">
              <User className="h-4 w-4 mr-2" />
              Perfil de Usuario
            </Button>
            
            <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
              <Bell className="h-4 w-4 mr-2" />
              Notificaciones
            </Button>
            
            <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
              <Shield className="h-4 w-4 mr-2" />
              Seguridad
            </Button>
            
            <Button variant="outline" className="w-full justify-start border-border text-card-foreground hover:bg-muted">
              <Database className="h-4 w-4 mr-2" />
              Sistema
            </Button>
          </div>

          {/* Settings Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* User Profile Settings */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-card-foreground">
                  <User className="h-5 w-5" />
                  <span>Perfil de Usuario</span>
                </CardTitle>
                <CardDescription>
                  Administra tu información personal y preferencias de la cuenta
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="name" className="text-card-foreground">Nombre Completo</Label>
                    <Input id="name" placeholder="Tu nombre completo" className="bg-input border-border text-card-foreground" />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-card-foreground">Correo Electrónico</Label>
                    <Input id="email" type="email" placeholder="tu@email.com" className="bg-input border-border text-card-foreground" />
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="role" className="text-card-foreground">Rol del Usuario</Label>
                  <Input id="role" value="Administrador" disabled className="bg-input border-border text-card-foreground opacity-50" />
                </div>
                
                <Button className="bg-success text-success-foreground hover:bg-success/90">
                  <Save className="h-4 w-4 mr-2" />
                  Guardar Cambios
                </Button>
              </CardContent>
            </Card>

            {/* Notification Settings */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-card-foreground">
                  <Bell className="h-5 w-5" />
                  <span>Notificaciones</span>
                </CardTitle>
                <CardDescription>
                  Configura cómo y cuándo recibir notificaciones
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-card-foreground">Alertas de Vehículos</Label>
                    <p className="text-sm text-muted-foreground">
                      Recibir notificaciones sobre mantenimiento y problemas
                    </p>
                  </div>
                  <Switch />
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-card-foreground">Reportes Diarios</Label>
                    <p className="text-sm text-muted-foreground">
                      Resumen diario de actividades de la flota
                    </p>
                  </div>
                  <Switch />
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-card-foreground">Notificaciones Push</Label>
                    <p className="text-sm text-muted-foreground">
                      Recibir notificaciones en tiempo real
                    </p>
                  </div>
                  <Switch />
                </div>
              </CardContent>
            </Card>

            {/* System Settings */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-card-foreground">
                  <SettingsIcon className="h-5 w-5" />
                  <span>Configuración del Sistema</span>
                </CardTitle>
                <CardDescription>
                  Configuración avanzada del sistema FleetGuard360
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="timezone" className="text-card-foreground">Zona Horaria</Label>
                    <Input id="timezone" value="UTC-05:00 (America/Lima)" disabled className="bg-input border-border text-card-foreground opacity-50" />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="language" className="text-card-foreground">Idioma</Label>
                    <Input id="language" value="Español (ES)" disabled className="bg-input border-border text-card-foreground opacity-50" />
                  </div>
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-card-foreground">Modo Mantenimiento</Label>
                    <p className="text-sm text-muted-foreground">
                      Activar para realizar mantenimiento del sistema
                    </p>
                  </div>
                  <Switch />
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </Navigation>
  );
};