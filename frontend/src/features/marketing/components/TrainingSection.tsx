import { FC } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Microscope, Leaf, BookOpen, HeartHandshake, type LucideIcon } from 'lucide-react';
import { Container } from '../../../components/layout/Container';
import { Card } from '../../../components/ui/Card';
import { TRAINING_HIGHLIGHTS } from '../data/marketingContent';

const ICON_MAP: Record<string, LucideIcon> = {
  Microscope,
  Leaf,
  BookOpen,
  HeartHandshake,
};

/**
 * TrainingSection
 *
 * Presentation-layer marketing section.
 * Uses static TRAINING_HIGHLIGHTS from marketingContent.ts.
 *
 * When a dedicated training API endpoint is available,
 * replace the static data source with a useTrainingPrograms() hook
 * and add the corresponding loading/error/empty states.
 */
export const TrainingSection: FC = () => {
  return (
    <section
      aria-label="Mushroom Cultivation Training"
      style={{
        padding: 'clamp(3rem, 8vw, 6rem) 0',
        background: 'var(--bg-secondary)',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Decorative accent — aria-hidden */}
      <div
        aria-hidden="true"
        style={{
          position: 'absolute',
          bottom: '-5%',
          left: '-5%',
          width: '400px',
          height: '400px',
          background: 'radial-gradient(circle, rgba(16,185,129,0.08) 0%, transparent 70%)',
          pointerEvents: 'none',
        }}
      />

      <Container>
        {/* Header */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 400px), 1fr))',
            gap: 'clamp(2rem, 5vw, 4rem)',
            alignItems: 'center',
            marginBottom: '3rem',
          }}
        >
          <div>
            <div
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.4rem',
                background: 'rgba(16,185,129,0.1)',
                border: '1px solid rgba(16,185,129,0.25)',
                borderRadius: 'var(--radius-full)',
                padding: '0.3rem 0.9rem',
                fontSize: '0.75rem',
                fontWeight: 700,
                color: 'var(--accent-primary)',
                letterSpacing: '0.05em',
                textTransform: 'uppercase',
                marginBottom: '1rem',
              }}
            >
              <BookOpen size={12} aria-hidden="true" />
              Cultivation Training
            </div>
            <h2
              style={{
                fontSize: 'clamp(1.5rem, 4vw, 2.25rem)',
                fontWeight: 800,
                color: 'var(--text-primary)',
                margin: 0,
                lineHeight: 1.2,
              }}
            >
              Learn to Grow with Confidence
            </h2>
            <p
              style={{
                fontSize: '1rem',
                color: 'var(--text-secondary)',
                lineHeight: 1.7,
                marginTop: '0.75rem',
                maxWidth: '480px',
              }}
            >
              SPOREKART training programs guide you from first agar plate through to a
              profitable commercial harvest — step by step, backed by working mycologists.
            </p>
            <Link
              to="/products"
              className="btn btn-primary"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                marginTop: '1.5rem',
              }}
            >
              Explore Training Programs <ArrowRight size={18} aria-hidden="true" />
            </Link>
          </div>

          {/* Highlight stats — presentation only, verified claims */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
              gap: '1rem',
            }}
          >
            {[
              { stat: 'Beginner → Pro', label: 'Structured curriculum path' },
              { stat: 'Live Labs', label: 'Hands-on practical sessions' },
              { stat: '1:1 Mentorship', label: 'Direct access to mycologists' },
            ].map((item) => (
              <Card
                key={item.stat}
                style={{
                  textAlign: 'center',
                  padding: '1.5rem 1rem',
                  gap: '0.5rem',
                }}
              >
                <div
                  style={{
                    fontSize: '1.1rem',
                    fontWeight: 800,
                    color: 'var(--accent-primary)',
                  }}
                >
                  {item.stat}
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                  {item.label}
                </div>
              </Card>
            ))}
          </div>
        </div>

        {/* Training Highlights grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
            gap: '1.25rem',
          }}
        >
          {TRAINING_HIGHLIGHTS.map((item) => {
            const Icon = ICON_MAP[item.icon];
            return (
              <div
                key={item.id}
                data-testid="training-highlight"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.75rem',
                  padding: '1.5rem',
                  background: 'var(--bg-card)',
                  backdropFilter: 'blur(8px)',
                  border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-lg)',
                  boxShadow: 'var(--shadow-md)',
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
                    }}
                  >
                    <Icon size={22} style={{ color: 'var(--accent-primary)' }} aria-hidden />
                  </div>
                )}
                <h3
                  style={{
                    fontSize: '1rem',
                    fontWeight: 700,
                    color: 'var(--text-primary)',
                    margin: 0,
                  }}
                >
                  {item.title}
                </h3>
                <p
                  style={{
                    fontSize: '0.875rem',
                    color: 'var(--text-secondary)',
                    margin: 0,
                    lineHeight: 1.6,
                  }}
                >
                  {item.description}
                </p>
              </div>
            );
          })}
        </div>
      </Container>
    </section>
  );
};
