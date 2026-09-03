import { FC } from 'react';
import { Link } from 'react-router-dom';
import { FileQuestion, Home, ArrowLeft } from 'lucide-react';
import { PageShell } from '../components/layout/PageShell';

export const NotFoundPage: FC = () => {
  return (
    <PageShell>
      <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
        <div className="card" style={{ maxWidth: '540px', margin: '0 auto', padding: '3rem 2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.25rem' }}>
            <FileQuestion size={56} style={{ color: 'var(--accent-primary)' }} />
          </div>
          <h1 style={{ fontSize: '3.5rem', fontWeight: 800, color: 'var(--accent-primary)', lineHeight: 1, marginBottom: '0.5rem' }}>
            404
          </h1>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--text-primary)' }}>
            Page Not Found
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem', lineHeight: 1.6 }}>
            The requested page route does not exist or may have been relocated.
          </p>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
            <Link to="/" className="btn btn-primary">
              <Home size={18} /> Return to Home
            </Link>
            <button onClick={() => window.history.back()} className="btn btn-secondary">
              <ArrowLeft size={18} /> Go Back
            </button>
          </div>
        </div>
      </div>
    </PageShell>
  );
};
