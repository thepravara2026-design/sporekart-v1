import { createContext, useContext, useState, useEffect, FC, ReactNode } from 'react';
import { authApi, LoginRequestDto } from '../services/authApi';

export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  roles: string[];
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequestDto) => Promise<void>;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    try {
      const savedToken = localStorage.getItem('token');
      const savedUser = localStorage.getItem('sporekart_user');
      if (savedToken && savedUser) {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      }
    } catch {
      localStorage.removeItem('token');
      localStorage.removeItem('sporekart_user');
    } finally {
      setIsLoading(false);
    }
  }, []);

  const saveAuth = (newToken: string, newUser: User) => {
    setToken(newToken);
    setUser(newUser);
    localStorage.setItem('token', newToken);
    localStorage.setItem('sporekart_user', JSON.stringify(newUser));
  };

  const login = async (credentials: LoginRequestDto) => {
    setIsLoading(true);
    try {
      const res = await authApi.login(credentials);
      const userObj: User = {
        id: res.userId,
        name: res.email.split('@')[0],
        email: res.email,
        role: res.role,
        roles: [res.role],
      };
      saveAuth(res.accessToken, userObj);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('sporekart_user');
  };

  const hasRole = (role: string): boolean => {
    if (!user) return false;
    if (user.role === role || user.roles?.includes(role)) return true;
    if (user.role === 'ROLE_ADMIN' || user.roles?.includes('ROLE_ADMIN')) return true;
    return false;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
