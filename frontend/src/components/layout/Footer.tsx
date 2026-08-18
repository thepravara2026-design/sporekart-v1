import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Sprout, ShieldCheck, Truck, Headphones } from 'lucide-react';

export const Footer: FC = () => {
  return (
    <footer
      role="contentinfo"
      aria-label="Site footer"
      style={{
        backgroundColor: 'var(--bg-secondary)',
        borderTop: '1px solid var(--border-color)',
        padding: '3rem 1.5rem 2rem 1.5rem',
        marginTop: 'auto',
      }}
    >
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        {/* Value Proposition Highlights */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: '1.5rem',
            paddingBottom: '2.5rem',
            marginBottom: '2.5rem',
            borderBottom: '1px solid var(--border-color)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Sprout size={24} style={{ color: 'var(--accent-primary)' }} />
            <div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>High-Yield Spawn</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Sterile certified mushroom batches</div>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Truck size={24} style={{ color: 'var(--accent-primary)' }} />
            <div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>Cold-Chain Express</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Temperature controlled shipping</div>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <ShieldCheck size={24} style={{ color: 'var(--accent-primary)' }} />
            <div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>Grower Guarantee</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>100% batch viability guarantee</div>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Headphones size={24} style={{ color: 'var(--accent-primary)' }} />
            <div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>Expert Cultivator Support</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>24/7 mycology technical help</div>
            </div>
          </div>
        </div>

        {/* Footer Navigation Columns */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            gap: '2rem',
            marginBottom: '2.5rem',
          }}
        >
          <div>
            <div style={{ fontWeight: 800, fontSize: '1.25rem', color: 'var(--accent-primary)', marginBottom: '0.75rem' }}>
              SPOREKART
            </div>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
              India's premier mushroom spawn, substrate equipment, and professional grower training platform.
            </p>
          </div>

          <div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem', marginBottom: '1rem', textTransform: 'uppercase', color: 'var(--text-primary)' }}>
              Catalog
            </div>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <li><Link to="/products" className="nav-link" style={{ fontSize: '0.875rem' }}>Oyster Spawn</Link></li>
              <li><Link to="/products" className="nav-link" style={{ fontSize: '0.875rem' }}>Button Spawn</Link></li>
              <li><Link to="/products" className="nav-link" style={{ fontSize: '0.875rem' }}>Substrates & Bags</Link></li>
              <li><Link to="/categories" className="nav-link" style={{ fontSize: '0.875rem' }}>All Categories</Link></li>
            </ul>
          </div>

          <div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem', marginBottom: '1rem', textTransform: 'uppercase', color: 'var(--text-primary)' }}>
              Training
            </div>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <li><Link to="/health" className="nav-link" style={{ fontSize: '0.875rem' }}>Grower Workshops</Link></li>
              <li><Link to="/design-system-showcase" className="nav-link" style={{ fontSize: '0.875rem' }}>Certification Specs</Link></li>
            </ul>
          </div>

          <div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem', marginBottom: '1rem', textTransform: 'uppercase', color: 'var(--text-primary)' }}>
              Platform
            </div>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <li><Link to="/health" className="nav-link" style={{ fontSize: '0.875rem' }}>System Health</Link></li>
              <li><Link to="/design-system-showcase" className="nav-link" style={{ fontSize: '0.875rem' }}>Design System Foundation</Link></li>
            </ul>
          </div>
        </div>

        {/* Copyright */}
        <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '1.5rem', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
          &copy; {new Date().getFullYear()} Sporekart v3.0. All rights reserved. Built with FAANG-grade React & Spring Boot.
        </div>
      </div>
    </footer>
  );
};
