import React from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { LogOut, User, Truck, Shield, BarChart3, Settings, AlertTriangle } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { NavLink, useLocation } from 'react-router-dom';

const navigationItems = {
  administrador: [
    { path: '/fleet', label: 'Gestión de Flota', icon: Truck },
    { path: '/alerts', label: 'Alertas', icon: AlertTriangle },
    { path: '/reports', label: 'Reportes', icon: BarChart3 },
    { path: '/settings', label: 'Configuración', icon: Settings },
  ],
  operador: [
    { path: '/alerts', label: 'Panel de Alertas', icon: AlertTriangle },
    { path: '/fleet', label: 'Flota (Solo lectura)', icon: Truck },
  ]
};

interface NavigationProps {
  children: React.ReactNode;
}

export const Navigation: React.FC<NavigationProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const location = useLocation();

  if (!user) return null;

  const userNavItems = navigationItems[user.role] || [];

  return (
    <div className="flex min-h-screen w-full bg-background">
      {/* Sidebar */}
      <div className="w-64 bg-sidebar border-r border-sidebar-border flex flex-col">
        {/* User Profile Section */}
        <div className="p-6 border-b border-sidebar-border">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-sidebar-accent rounded-full flex items-center justify-center">
              <User className="h-5 w-5 text-sidebar-accent-foreground" />
            </div>
            <div>
              <p className="text-sidebar-foreground font-medium">{user?.name || 'Nombre Usuario'}</p>
              <Badge 
                variant={user?.role === 'administrador' ? 'default' : 'secondary'}
                className={user?.role === 'administrador' 
                  ? 'bg-sidebar-primary text-sidebar-primary-foreground text-xs' 
                  : 'bg-sidebar-accent text-sidebar-accent-foreground text-xs'
                }
              >
                {user?.role === 'administrador' ? 'Admin' : 'Operador'}
              </Badge>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4">
          <div className="space-y-2">
            {userNavItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) =>
                    `flex items-center space-x-3 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-sidebar-primary text-sidebar-primary-foreground'
                        : 'text-sidebar-foreground hover:text-sidebar-primary-foreground hover:bg-sidebar-accent'
                    }`
                  }
                >
                  <Icon className="h-4 w-4" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </div>
        </nav>

        {/* Logout Button */}
        <div className="p-4 border-t border-sidebar-border">
          <Button
            variant="outline"
            onClick={logout}
            className="w-full justify-start bg-sidebar-accent border-sidebar-border text-sidebar-accent-foreground hover:bg-sidebar-accent/80"
          >
            <LogOut className="h-4 w-4 mr-2" />
            Cerrar Sesión
          </Button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Header */}
        <header className="bg-card border-b border-border px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <Shield className="h-5 w-5 text-primary-foreground" />
              </div>
              <h1 className="text-xl font-bold text-card-foreground">FleetGuard360</h1>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 p-6 bg-background">
          {children}
        </main>
      </div>
    </div>
  );
};