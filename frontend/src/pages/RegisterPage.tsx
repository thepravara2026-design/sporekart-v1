import { FC, useState, FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi, RegisterRequestDto } from '../services/authApi';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { UserPlus, KeyRound, AlertCircle, CheckCircle } from 'lucide-react';

export const RegisterPage: FC = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) {
    navigate('/', { replace: true });
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Email and password are required.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    setError(null);
    setSuccess(null);
    setLoading(true);
    try {
      const payload: RegisterRequestDto = {
        email,
        password,
        firstName: firstName || undefined,
        lastName: lastName || undefined,
      };
      await authApi.register(payload);
      setSuccess('Account created successfully. Please sign in.');
      navigate('/login', { state: { registered: true } });
    } catch {
      setError('Unable to create your account. The email may already be registered.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '480px', margin: '4rem auto', padding: '0 1rem' }}>
      <Card>
        <CardHeader style={{ textAlign: 'center' }}>
          <div style={{ display: 'inline-flex', padding: '12px', background: '#ecfdf5', borderRadius: '50%', color: '#059669', marginBottom: '1rem' }}>
            <UserPlus size={32} />
          </div>
          <CardTitle>Create your Sporekart account</CardTitle>
          <CardDescription>Join Sporekart to shop, track orders, and manage your profile</CardDescription>
        </CardHeader>

        <CardContent>
          {error && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', background: '#fef2f2', color: '#dc2626', borderRadius: '6px', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', background: '#ecfdf5', color: '#059669', borderRadius: '6px', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              <CheckCircle size={18} />
              <span>{success}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div>
                <label htmlFor="firstName" style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '6px' }}>
                  First Name
                </label>
                <Input
                  id="firstName"
                  placeholder="A."
                  value={firstName}
                  onChange={e => setFirstName(e.target.value)}
                  autoComplete="given-name"
                />
              </div>
              <div>
                <label htmlFor="lastName" style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '6px' }}>
                  Last Name
                </label>
                <Input
                  id="lastName"
                  placeholder="Buyer"
                  value={lastName}
                  onChange={e => setLastName(e.target.value)}
                  autoComplete="family-name"
                />
              </div>
            </div>

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
                autoComplete="email"
              />
            </div>

            <div>
              <label htmlFor="password" style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '6px' }}>
                Password
              </label>
              <Input
                id="password"
                type="password"
                placeholder="Create a password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
                minLength={8}
                autoComplete="new-password"
              />
            </div>

            <div>
              <label htmlFor="confirmPassword" style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '6px' }}>
                Confirm Password
              </label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="Repeat your password"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
                minLength={8}
                autoComplete="new-password"
              />
            </div>

            <Button type="submit" variant="primary" style={{ width: '100%', marginTop: '0.5rem' }} disabled={loading}>
              <KeyRound size={18} style={{ marginRight: '8px' }} />
              {loading ? 'Creating account...' : 'Create Account'}
            </Button>
          </form>

          <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Already have an account? </span>
            <Link to="/login" style={{ fontSize: '0.875rem', color: '#059669', textDecoration: 'none', fontWeight: 600 }}>
              Sign in
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