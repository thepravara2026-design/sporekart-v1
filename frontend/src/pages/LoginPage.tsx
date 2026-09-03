import { FC, useState, FormEvent } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Shield, KeyRound, UserCheck, AlertCircle } from 'lucide-react';

export const LoginPage: FC = () => {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/';

  if (isAuthenticated) {
    navigate(from, { replace: true });
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch {
      setError('Authentication failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = async (presetEmail: string) => {
    setEmail(presetEmail);
    setPassword('password123');
    setError(null);
    setLoading(true);
    try {
      await login({ email: presetEmail, password: 'password123' });
      navigate(from, { replace: true });
    } catch {
      setError('Real backend login failed for quick fill.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '480px', margin: '4rem auto', padding: '0 1rem' }}>
      <Card>
        <CardHeader style={{ textAlign: 'center' }}>
          <div style={{ display: 'inline-flex', padding: '12px', background: '#ecfdf5', borderRadius: '50%', color: '#059669', marginBottom: '1rem' }}>
            <Shield size={32} />
          </div>
          <CardTitle>Sign in to Sporekart</CardTitle>
          <CardDescription>Enter your credentials to access your portal</CardDescription>
        </CardHeader>

        <CardContent>
          {error && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', background: '#fef2f2', color: '#dc2626', borderRadius: '6px', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <label htmlFor="email" style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '6px' }}>
                Email Address
              </label>
              <Input
                id="email"
                type="email"
                placeholder="name@sporekart.com"
                value={email}
                onChange={e => setEmail(e.target.value)}
                required
              />
            </div>

            <div>
              <label htmlFor="password" style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '6px' }}>
                Password
              </label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
              />
            </div>

            <Button type="submit" variant="primary" style={{ width: '100%', marginTop: '0.5rem' }} disabled={loading}>
              <KeyRound size={18} style={{ marginRight: '8px' }} />
              {loading ? 'Authenticating...' : 'Sign In'}
            </Button>
          </form>

          <div style={{ margin: '2rem 0 1rem', borderTop: '1px solid #e2e8f0', paddingTop: '1.5rem', textAlign: 'center' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#64748b', textTransform: 'uppercase', letterSpacing: '1px' }}>
              Seeded Dev Accounts
            </span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <Button variant="outline" onClick={() => handleQuickFill('admin1@sporekart.com')} style={{ justifyContent: 'flex-start', fontSize: '0.8125rem' }}>
              <UserCheck size={14} style={{ marginRight: '6px', color: '#dc2626' }} />
              Admin
            </Button>

            <Button variant="outline" onClick={() => handleQuickFill('grower@sporekart.com')} style={{ justifyContent: 'flex-start', fontSize: '0.8125rem' }}>
              <UserCheck size={14} style={{ marginRight: '6px', color: '#16a34a' }} />
              Grower
            </Button>

            <Button variant="outline" onClick={() => handleQuickFill('seller@sporekart.com')} style={{ justifyContent: 'flex-start', fontSize: '0.8125rem' }}>
              <UserCheck size={14} style={{ marginRight: '6px', color: '#0284c7' }} />
              Seller
            </Button>

            <Button variant="outline" onClick={() => handleQuickFill('trainee@sporekart.com')} style={{ justifyContent: 'flex-start', fontSize: '0.8125rem' }}>
              <UserCheck size={14} style={{ marginRight: '6px', color: '#2563eb' }} />
              Trainee
            </Button>
          </div>

          <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>New to Sporekart? </span>
            <Link to="/register" style={{ fontSize: '0.875rem', color: '#059669', textDecoration: 'none', fontWeight: 600 }}>
              Create an account
            </Link>
          </div>
          <div style={{ textAlign: 'center', marginTop: '0.75rem' }}>
            <Link to="/" style={{ fontSize: '0.875rem', color: '#059669', textDecoration: 'none' }}>
              ← Return to Storefront
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
