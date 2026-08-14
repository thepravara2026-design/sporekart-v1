import { FC } from 'react';
import { Link } from 'react-router-dom';

export const NotFoundPage: FC = () => {
  return (
    <div style={{ textAlign: 'center', padding: '4rem 1rem' }}>
      <div className="card" style={{ maxWidth: '500px', margin: '0 auto' }}>
        <h1 style={{ fontSize: '4rem', color: 'var(--accent-primary)', lineHeight: 1 }}>404</h1>
        <h2 style={{ margin: '1rem 0' }}>Page Not Found</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
          The requested page route does not exist or has been moved.
        </p>
        <Link to="/" className="btn btn-primary">
          Return to Home
        </Link>
      </div>
    </div>
  );
};
