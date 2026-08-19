import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Sprout, ShieldCheck, Truck, Headphones } from 'lucide-react';
import { FOOTER_NAVIGATION } from '../../config/navigation';

export const Footer: FC = () => {
  const year = new Date().getFullYear();

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
        {/* Value Proposition Strip */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1.25rem',
            paddingBottom: '2.5rem',
            marginBottom: '2.5rem',
            borderBottom: '1px solid var(--border-color)',
          }}
        >
          {[
            {
              icon: <Sprout size={24} style={{ color: 'var(--accent-primary)' }} aria-hidden="true" />,
              title: 'High-Yield Spawn',
              desc: 'Sterile certified mushroom batches',
            },
            {
              icon: <Truck size={24} style={{ color: 'var(--accent-primary)' }} aria-hidden="true" />,
              title: 'Cold-Chain Express',
              desc: 'Temperature-controlled shipping',
            },
            {
              icon: <ShieldCheck size={24} style={{ color: 'var(--accent-primary)' }} aria-hidden="true" />,
              title: 'Batch Viability Guarantee',
              desc: 'Non-viable batches replaced at no charge',
            },
            {
              icon: <Headphones size={24} style={{ color: 'var(--accent-primary)' }} aria-hidden="true" />,
              title: 'Expert Cultivator Support',
              desc: 'Mycology technical help from professionals',
            },
          ].map((item) => (
            <div
              key={item.title}
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: '0.85rem',
                padding: '1rem 1.25rem',
                background: 'rgba(255, 255, 255, 0.03)',
                border: '1px solid var(--border-subtle)',
                borderRadius: 'var(--radius-lg)',
                transition: 'border-color var(--transition-fast), background-color var(--transition-fast)',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '2.75rem',
                  height: '2.75rem',
                  flexShrink: 0,
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(16, 185, 129, 0.1)',
                  border: '1px solid rgba(16, 185, 129, 0.25)',
                }}
              >
                {item.icon}
              </div>
              <div>
                <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>{item.title}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>{item.desc}</div>
              </div>
            </div>
          ))}
        </div>

        {/* Navigation Columns */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
            gap: '2rem',
            marginBottom: '2.5rem',
          }}
        >
          {/* Brand column */}
          <div>
            <div
              style={{
                fontWeight: 800,
                fontSize: '1.25rem',
                color: 'var(--accent-primary)',
                marginBottom: '0.75rem',
              }}
            >
              SPOREKART
            </div>
            <p
              style={{
                fontSize: '0.875rem',
                color: 'var(--text-secondary)',
                lineHeight: 1.6,
                margin: 0,
              }}
            >
              India's premier mushroom spawn, substrate, and professional grower training platform.
            </p>
          </div>

          {/* Dynamic nav columns from centralized config */}
          {FOOTER_NAVIGATION.map((group) => (
            <nav key={group.heading} aria-label={`Footer — ${group.heading}`}>
              <div
                style={{
                  fontWeight: 700,
                  fontSize: '0.875rem',
                  marginBottom: '1rem',
                  textTransform: 'uppercase',
                  letterSpacing: '0.06em',
                  color: 'var(--text-primary)',
                }}
              >
                {group.heading}
              </div>
              <ul
                style={{
                  listStyle: 'none',
                  padding: 0,
                  margin: 0,
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.5rem',
                }}
              >
                {group.links.map((link) => (
                  <li key={link.path}>
                    <Link
                      to={link.path}
                      className="nav-link"
                      style={{ fontSize: '0.875rem' }}
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </nav>
          ))}
        </div>

        {/* Copyright */}
        <div
          style={{
            borderTop: '1px solid var(--border-color)',
            paddingTop: '1.5rem',
            textAlign: 'center',
            fontSize: '0.825rem',
            color: 'var(--text-secondary)',
          }}
        >
          &copy; {year} SPOREKART. All rights reserved.
        </div>
      </div>
    </footer>
  );
};
