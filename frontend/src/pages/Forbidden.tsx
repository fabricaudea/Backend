import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { ShieldX, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';

export const Forbidden: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleGoBack = () => {
    // Redirect based on user role
    if (user?.role === 'administrador') {
      navigate('/fleet', { replace: true });
    } else {
      navigate('/alerts', { replace: true });
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md text-center">
        <CardHeader>
          <div className="mx-auto w-16 h-16 bg-destructive/10 rounded-full flex items-center justify-center mb-4">
            <ShieldX className="h-8 w-8 text-destructive" />
          </div>
          <CardTitle className="text-2xl font-bold text-foreground">
            403 - Acceso Denegado
          </CardTitle>
          <CardDescription className="text-muted-foreground">
            No tienes permisos para acceder a esta página.
          </CardDescription>
        </CardHeader>
        
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Tu rol actual ({user?.role === 'administrador' ? 'Administrador' : 'Operador'}) no tiene 
            los permisos necesarios para ver este contenido.
          </p>
          
          <Button
            onClick={handleGoBack}
            className="w-full"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver al inicio
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};