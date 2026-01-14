import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Eye, EyeOff, Truck } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { Navigate, useNavigate } from 'react-router-dom';

export const Login: React.FC = () => {
  const { login, user, isLoading } = useAuth();
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<{ username?: string; password?: string }>({});

  if (user) {
    // Redirect based on user role
    const redirectPath = user.role === 'administrador' ? '/fleet' : '/alerts';
    return <Navigate to={redirectPath} replace />;
  }

  const validateForm = () => {
    const errors: { username?: string; password?: string } = {};
    
    if (!credentials.username.trim()) {
      errors.username = 'El usuario es requerido';
    }
    
    if (!credentials.password.trim()) {
      errors.password = 'La contraseña es requerida';
    }
    
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!validateForm()) return;

    const success = await login(credentials);
    if (!success) {
      setError('Usuario o contraseña incorrectos');
    } else {
      // Login successful, user will be redirected by the Navigate component above
    }
  };

  const handleInputChange = (field: string, value: string) => {
    setCredentials(prev => ({ ...prev, [field]: value }));
    setError('');
    if (fieldErrors[field as keyof typeof fieldErrors]) {
      setFieldErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto w-16 h-16 bg-primary rounded-full flex items-center justify-center mb-4">
            <Truck className="h-8 w-8 text-primary-foreground" />
          </div>
          <CardTitle className="text-2xl font-bold text-foreground">FleetGuard360</CardTitle>
          <CardDescription className="text-muted-foreground">
            Ingresa tus credenciales para acceder al sistema
          </CardDescription>
        </CardHeader>

        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            {error && (
              <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <div className="space-y-2">
              <Label htmlFor="username">Usuario</Label>
              <Input
                id="username"
                type="text"
                value={credentials.username}
                onChange={(e) => handleInputChange('username', e.target.value)}
                placeholder="Ingresa tu usuario"
                disabled={isLoading}
                className={fieldErrors.username ? 'border-destructive' : ''}
                aria-describedby={fieldErrors.username ? 'username-error' : undefined}
                autoComplete="username"
              />
              {fieldErrors.username && (
                <p id="username-error" className="text-sm text-destructive" role="alert">
                  {fieldErrors.username}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Contraseña</Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  value={credentials.password}
                  onChange={(e) => handleInputChange('password', e.target.value)}
                  placeholder="Ingresa tu contraseña"
                  disabled={isLoading}
                  className={fieldErrors.password ? 'border-destructive pr-10' : 'pr-10'}
                  aria-describedby={fieldErrors.password ? 'password-error' : undefined}
                  autoComplete="current-password"
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={isLoading}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4 text-muted-foreground" />
                  ) : (
                    <Eye className="h-4 w-4 text-muted-foreground" />
                  )}
                </Button>
              </div>
              {fieldErrors.password && (
                <p id="password-error" className="text-sm text-destructive" role="alert">
                  {fieldErrors.password}
                </p>
              )}
            </div>
          </CardContent>

          <CardFooter className="flex flex-col space-y-4">
            <Button
              type="submit"
              className="w-full bg-primary text-primary-foreground hover:bg-primary-hover"
              disabled={isLoading}
            >
              {isLoading ? 'Iniciando sesión...' : 'Iniciar sesión'}
            </Button>
            
            <Button
              type="button"
              variant="link"
              className="text-primary hover:text-primary-hover"
              disabled={isLoading}
            >
              ¿Olvidó su contraseña?
            </Button>
          </CardFooter>
        </form>

        {/* Demo credentials */}
        <div className="px-6 pb-6">
          <div className="text-xs text-muted-foreground space-y-1">
            <p className="font-medium">Credenciales de prueba:</p>
            <p>• Usuario: <code className="bg-muted px-1 rounded">admin</code> / Contraseña: <code className="bg-muted px-1 rounded">admin123</code></p>
            <p>• Usuario: <code className="bg-muted px-1 rounded">operador</code> / Contraseña: <code className="bg-muted px-1 rounded">operador123</code></p>
          </div>
        </div>
      </Card>
    </div>
  );
};