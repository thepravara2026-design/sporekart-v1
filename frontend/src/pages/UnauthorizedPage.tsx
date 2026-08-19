import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { ShieldAlert, Home, ArrowLeft } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const UnauthorizedPage: FC = () => {
  const { user } = useAuth();

  return (
    <div style={{ maxWidth: '520px', margin: '5rem auto', padding: '0 1rem', textAlign: 'center' }}>
      <Card>
        <CardHeader>
          <div style={{ display: 'inline-flex', padding: '16px', background: '#fef2f2', borderRadius: '50%', color: '#dc2626', marginBottom: '1rem', margin: '0 auto 1rem' }}>
            <ShieldAlert size={40} />
          </div>
          <CardTitle style={{ fontSize: '1.5rem', color: '#1e293b' }}>403 — Access Forbidden</CardTitle>
          <CardDescription>
            You do not have the required permissions to access this area.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p style={{ fontSize: '0.875rem', color: '#64748b', marginBottom: '1.5rem' }}>
            Your current account <strong style={{ color: '#0f172a' }}>{user?.email || 'Guest'}</strong> (Role: <span style={{ fontFamily: 'monospace', color: '#2563eb' }}>{user?.role || 'NONE'}</span>) is restricted from viewing this protected resource.
          </p>

          <div style={{ display: 'flex', gap: '12px', justifyContent: 'center' }}>
            <Link to="/" style={{ textDecoration: 'none' }}>
              <Button variant="outline">
                <Home size={16} style={{ marginRight: '6px' }} />
                Home
              </Button>
            </Link>
            <Link to="/login" style={{ textDecoration: 'none' }}>
              <Button variant="primary">
                <ArrowLeft size={16} style={{ marginRight: '6px' }} />
                Switch Persona
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
