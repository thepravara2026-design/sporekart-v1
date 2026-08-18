import { FC } from 'react';
import {
  Sprout, Truck, ShieldCheck, Headphones, Lock, RefreshCw, type LucideIcon,
} from 'lucide-react';
import { Container } from '../../../components/layout/Container';
import { TRUST_SIGNALS } from '../data/marketingContent';

const ICON_MAP: Record<string, LucideIcon> = {
  Sprout,
  Truck,
  ShieldCheck,
  Headphones,
  Lock,
  RefreshCw,
};

export const TrustSection: FC = () => {
  return (
    <section
      aria-label="Why Growers Trust SPOREKART"
      style={{
        padding: 'clamp(3rem, 8vw, 6rem) 0',
        background: 'var(--bg-primary)',
      }}
    >
      <Container>
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
          <h2
            style={{
              fontSize: 'clamp(1.5rem, 4vw, 2.25rem)',
              fontWeight: 800,
              color: 'var(--text-primary)',
              margin: 0,
            }}
          >
            Built for Serious Growers
          </h2>
          <p
            style={{
              fontSize: '1rem',
              color: 'var(--text-secondary)',
              marginTop: '0.5rem',
              maxWidth: '540px',
              margin: '0.5rem auto 0 auto',
            }}
          >
            Every SPOREKART order ships with confidence, backed by quality controls and
            a dedicated support team.
          </p>
        </div>

        {/* Trust signal cards */}
        <div
          data-testid="trust-signals-grid"
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
            gap: '1.25rem',
          }}
        >
          {TRUST_SIGNALS.map((signal) => {
            const Icon = ICON_MAP[signal.icon];
            return (
              <div
                key={signal.id}
                data-testid="trust-signal-item"
                style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '1rem',
                  padding: '1.5rem',
                  background: 'var(--bg-card)',
                  backdropFilter: 'blur(8px)',
                  border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-lg)',
                  boxShadow: 'var(--shadow-sm)',
                }}
              >
                {Icon && (
                  <div
                    style={{
                      width: '44px',
                      height: '44px',
                      background: 'rgba(16,185,129,0.12)',
                      border: '1px solid rgba(16,185,129,0.2)',
                      borderRadius: 'var(--radius-md)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0,
                    }}
                  >
                    <Icon
                      size={22}
                      style={{ color: 'var(--accent-primary)' }}
                      aria-hidden
                    />
                  </div>
                )}
                <div>
                  <h3
                    style={{
                      fontSize: '0.95rem',
                      fontWeight: 700,
                      color: 'var(--text-primary)',
                      margin: '0 0 0.3rem 0',
                    }}
                  >
                    {signal.title}
                  </h3>
                  <p
                    style={{
                      fontSize: '0.85rem',
                      color: 'var(--text-secondary)',
                      margin: 0,
                      lineHeight: 1.6,
                    }}
                  >
                    {signal.description}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </Container>
    </section>
  );
};
