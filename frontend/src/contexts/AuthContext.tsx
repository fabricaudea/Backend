import React, { createContext, useContext, useState, useEffect } from 'react';
import { AuthContextType, LoginCredentials, User } from '@/types/vehicle';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Mock users for demonstration
const mockUsers: { [key: string]: { password: string; user: User } } = {
  admin: {
    password: 'admin123',
    user: {
      id: '1',
      username: 'admin',
      role: 'administrador',
      name: 'Administrador FleetGuard'
    }
  },
  operador: {
    password: 'op123',
    user: {
      id: '2',
      username: 'operador',
      role: 'operador',
      name: 'Operador de Flota'
    }
  }
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in on app start
    const savedUser = localStorage.getItem('fleetguard_user');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (error) {
        localStorage.removeItem('fleetguard_user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (credentials: LoginCredentials): Promise<boolean> => {
    setIsLoading(true);
    
    // Simulate API call delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const mockUser = mockUsers[credentials.username];
    
    if (mockUser && mockUser.password === credentials.password) {
      setUser(mockUser.user);
      localStorage.setItem('fleetguard_user', JSON.stringify(mockUser.user));
      setIsLoading(false);
      return true;
    }
    
    setIsLoading(false);
    return false;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('fleetguard_user');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};