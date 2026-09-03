import { FC } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Sprout, Leaf } from 'lucide-react';

export const HeroSection: FC = () => {
  return (
    <section
      aria-label="Hero — Welcome to SPOREKART"
      style={{
        background:
          'linear-gradient(160deg, rgba(5,28,20,0.95) 0%, rgba(10,41,29,0.85) 50%, rgba(5,28,20,0.98) 100%)',
        borderBottom: '1px solid var(--border-color)',
        padding: 'clamp(4rem, 10vw, 8rem) 1.5rem',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Decorative radial glow — aria-hidden, purely visual */}
      <div
        aria-hidden="true"
        style={{
          position: 'absolute',
          top: '-10%',
          right: '-5%',
          width: 'clamp(300px, 50vw, 700px)',
          height: 'clamp(300px, 50vw, 700px)',
          background: 'radial-gradient(circle, rgba(16,185,129,0.12) 0%, transparent 70%)',
          pointerEvents: 'none',
        }}
      />

      <div style={{ maxWidth: '1200px', margin: '0 auto', position: 'relative' }}>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 440px), 1fr))',
            gap: 'clamp(2rem, 6vw, 5rem)',
            alignItems: 'center',
          }}
        >
          {/* Left: Copy */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            {/* Eyebrow badge */}
            <div
              aria-label="India's premier mushroom cultivation platform"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                background: 'rgba(16,185,129,0.12)',
                border: '1px solid rgba(16,185,129,0.3)',
                borderRadius: 'var(--radius-full)',
                padding: '0.35rem 1rem',
                fontSize: '0.8rem',
                fontWeight: 700,
                color: 'var(--accent-primary)',
                letterSpacing: '0.04em',
                textTransform: 'uppercase',
                width: 'fit-content',
              }}
            >
              <Sprout size={14} aria-hidden="true" />
              India's Premier Mushroom Platform
            </div>

            <h1
              style={{
                fontSize: 'clamp(2rem, 5vw, 3.75rem)',
                fontWeight: 900,
                lineHeight: 1.1,
                letterSpacing: '-0.02em',
                color: 'var(--text-primary)',
                margin: 0,
              }}
            >
              Grow More.{' '}
              <span
                style={{
                  background: 'linear-gradient(135deg, #34d399, #10b981, #059669)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  backgroundClip: 'text',
                }}
              >
                Waste Less.
              </span>{' '}
              Harvest Consistently.
            </h1>

            <p
              style={{
                fontSize: 'clamp(1rem, 2.5vw, 1.2rem)',
                color: 'var(--text-secondary)',
                lineHeight: 1.7,
                maxWidth: '520px',
                margin: 0,
              }}
            >
              Premium mushroom spawn, high-performance substrates, and expert cultivation
              training — backed by science, delivered cold-chain fresh across India.
            </p>

            {/* CTAs */}
            <div
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                gap: '1rem',
                marginTop: '0.5rem',
              }}
            >
              <Link
                to="/products"
                className="btn btn-primary"
                style={{
                  padding: '0.85rem 1.85rem',
                  fontSize: '1.125rem',
                  borderRadius: 'var(--radius-md)',
                  fontWeight: 600,
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  minWidth: '180px',
                  justifyContent: 'center',
                  background: 'linear-gradient(135deg, #10b981, #059669)',
                  color: '#fff',
                  textDecoration: 'none',
                }}
              >
                Shop Spawn &amp; Substrate <ArrowRight size={18} aria-hidden="true" />
              </Link>
              <Link
                to="/categories"
                className="btn btn-secondary"
                style={{
                  padding: '0.85rem 1.85rem',
                  fontSize: '1.125rem',
                  borderRadius: 'var(--radius-md)',
                  fontWeight: 600,
                }}
              >
                Browse Categories
              </Link>
            </div>

            {/* Trust micro-signals */}
            <div
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                gap: '1.5rem',
                marginTop: '0.5rem',
              }}
            >
              {[
                'Cold-chain shipping',
                'Batch viability guarantee',
                'Expert grower support',
              ].map((signal) => (
                <div
                  key={signal}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.4rem',
                    fontSize: '0.85rem',
                    color: 'var(--text-secondary)',
                  }}
                >
                  <Leaf
                    size={14}
                    aria-hidden="true"
                    style={{ color: 'var(--accent-primary)', flexShrink: 0 }}
                  />
                  {signal}
                </div>
              ))}
            </div>
          </div>

          {/* Right: Visual specimen card */}
          <div
            aria-hidden="true"
            style={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
            }}
          >
            <div
              style={{
                background: 'var(--bg-card)',
                backdropFilter: 'blur(12px)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-xl)',
                padding: '2.5rem',
                boxShadow: 'var(--shadow-lg), var(--shadow-glow)',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '1.25rem',
                maxWidth: '340px',
                width: '100%',
              }}
            >
              <div
                style={{
                  width: '100%',
                  aspectRatio: '1',
                  borderRadius: 'var(--radius-lg)',
                  background:
                    'radial-gradient(circle at 35% 35%, rgba(16,185,129,0.25) 0%, rgba(5,28,20,0.95) 70%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  position: 'relative',
                }}
              >
                <Sprout
                  size={80}
                  style={{ color: 'var(--accent-primary)', opacity: 0.9 }}
                />
              </div>
              <div style={{ textAlign: 'center' }}>
                <div
                  style={{
                    fontSize: '1.1rem',
                    fontWeight: 800,
                    color: 'var(--text-primary)',
                    marginBottom: '0.3rem',
                  }}
                >
                  Premium Spawn Selection
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  Oyster · Button · Shiitake · Lion's Mane · Reishi
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
