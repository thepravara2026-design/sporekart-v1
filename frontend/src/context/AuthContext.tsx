import { createContext, useContext, useState, useEffect, FC, ReactNode } from 'react';
import { authApi, LoginRequestDto } from '../services/authApi';

export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  roles: string[];
}

export type PresetRoleType = 'admin' | 'grower' | 'trainee' | 'customer' | 'dual';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequestDto) => Promise<void>;
  loginWithPreset: (preset: PresetRoleType) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const PRESET_USERS: Record<PresetRoleType, { user: User; token: string }> = {
  admin: {
    user: {
      id: 'usr-admin-01',
      name: 'System Admin',
      email: 'admin@sporekart.com',
      role: 'ROLE_ADMIN',
      roles: ['ROLE_ADMIN'],
    },
    token: 'mock-jwt-admin-token',
  },
  grower: {
    user: {
      id: 'usr-grower-01',
      name: 'Preetham Bio Farms',
      email: 'grower@sporekart.com',
      role: 'ROLE_GROWER',
      roles: ['ROLE_GROWER', 'ROLE_SELLER'],
    },
    token: 'mock-jwt-grower-token',
  },
  trainee: {
    user: {
      id: 'usr-trainee-01',
      name: 'Ramesh Trainee',
      email: 'trainee@sporekart.com',
      role: 'ROLE_TRAINEE',
      roles: ['ROLE_TRAINEE'],
    },
    token: 'mock-jwt-trainee-token',
  },
  customer: {
    user: {
      id: 'usr-customer-01',
      name: 'Mushroom Cultivator',
      email: 'customer@sporekart.com',
      role: 'ROLE_CUSTOMER',
      roles: ['ROLE_CUSTOMER'],
    },
    token: 'mock-jwt-customer-token',
  },
  dual: {
    user: {
      id: 'usr-dual-01',
      name: 'Grower & Trainee Combined',
      email: 'grower.trainee@sporekart.com',
      role: 'ROLE_GROWER',
      roles: ['ROLE_GROWER', 'ROLE_TRAINEE', 'ROLE_SELLER'],
    },
    token: 'mock-jwt-dual-token',
  },
};

export const AuthProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    try {
      const savedToken = localStorage.getItem('accessToken');
      const savedUser = localStorage.getItem('sporekart_user');
      if (savedToken && savedUser) {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      }
    } catch {
      // Fallback cleanly on JSON error
      localStorage.removeItem('accessToken');
      localStorage.removeItem('sporekart_user');
    } finally {
      setIsLoading(false);
    }
  }, []);

  const saveAuth = (newToken: string, newUser: User) => {
    setToken(newToken);
    setUser(newUser);
    localStorage.setItem('accessToken', newToken);
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
    } catch (err) {
      if (!import.meta.env.DEV) {
        throw err;
      }
      // Fallback for dev mode login — only active in development builds
      const matchedPreset = credentials.email.includes('admin')
        ? PRESET_USERS.admin
        : credentials.email.includes('grower')
        ? PRESET_USERS.grower
        : credentials.email.includes('trainee')
        ? PRESET_USERS.trainee
        : PRESET_USERS.customer;
      saveAuth(matchedPreset.token, matchedPreset.user);
    } finally {
      setIsLoading(false);
    }
  };

  const loginWithPreset = (preset: PresetRoleType) => {
    const data = PRESET_USERS[preset];
    if (data) {
      saveAuth(data.token, data.user);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('token');
    localStorage.removeItem('sporekart_user');
  };

  const hasRole = (role: string): boolean => {
    if (!user) return false;
    if (user.role === role || user.roles?.includes(role)) return true;
    // System admin inherits all permissions
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
        loginWithPreset,
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
