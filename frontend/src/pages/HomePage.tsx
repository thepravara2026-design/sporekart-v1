import { FC } from 'react';
import { Link } from 'react-router-dom';

export const HomePage: FC = () => {
  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <header style={{ textAlign: 'center', margin: '2rem 0' }}>
        <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem', fontWeight: 800 }}>
          Sporekart Platform v3.0
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1.1rem' }}>
          Engineering Foundation, Architecture Baseline &amp; Developer Infrastructure
        </p>
      </header>

      <div className="card">
        <h2 style={{ marginBottom: '1rem', fontSize: '1.4rem' }}>Platform Architecture Baseline</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem' }}>
          Sporekart v3.0 is established as a production-grade <strong>Modular Monolith</strong> in a unified Monorepo, powered by Java 21 LTS, Spring Boot 3.4+, React Vite TypeScript, and Supabase PostgreSQL.
        </p>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginTop: '1.5rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
            <h3 style={{ color: 'var(--accent-primary)', marginBottom: '0.5rem' }}>Backend Stack</h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Java 21 • Spring Boot 3.4 • Spring Data JPA • Flyway • Spring Security</p>
          </div>
          <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
            <h3 style={{ color: 'var(--accent-primary)', marginBottom: '0.5rem' }}>Frontend Stack</h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>React 18 • TypeScript • Vite • TanStack Query • Axios • Vitest</p>
          </div>
        </div>

        <div style={{ marginTop: '2rem', textAlign: 'center' }}>
          <Link to="/health" className="btn btn-primary">
            View Live Health Status &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
};
