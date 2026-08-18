import { FC } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BookOpen } from 'lucide-react';
import { Container } from '../../../components/layout/Container';
import { GROWER_STORIES } from '../data/marketingContent';

/**
 * GrowerStories
 *
 * IMPORTANT: These story cards are PRESENTATION LAYER ONLY.
 * They use illustrative story summaries to demonstrate the component format.
 * They are NOT fabricated testimonials or fake customer quotes.
 *
 * When a real stories/blog API is available:
 * - Add a useGrowerStories() hook
 * - Replace static GROWER_STORIES with API-fetched data
 * - Add real images and author attribution
 */
export const GrowerStories: FC = () => {
  return (
    <section
      aria-label="Grower Stories"
      style={{
        padding: 'clamp(3rem, 8vw, 6rem) 0',
        background: 'var(--bg-secondary)',
      }}
    >
      <Container>
        {/* Header */}
        <div style={{ marginBottom: '2.5rem' }}>
          <h2
            style={{
              fontSize: 'clamp(1.5rem, 4vw, 2.25rem)',
              fontWeight: 800,
              color: 'var(--text-primary)',
              margin: 0,
            }}
          >
            Grower Stories
          </h2>
          <p
            style={{
              fontSize: '1rem',
              color: 'var(--text-secondary)',
              marginTop: '0.4rem',
            }}
          >
            How SPOREKART growers are cultivating with consistency.
          </p>
        </div>

        {/* Story cards */}
        <div
          data-testid="grower-stories-grid"
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(min(100%, 300px), 1fr))',
            gap: '1.5rem',
          }}
        >
          {GROWER_STORIES.map((story) => (
            <article
              key={story.id}
              data-testid="grower-story-card"
              aria-labelledby={`story-title-${story.id}`}
              style={{
                display: 'flex',
                flexDirection: 'column',
                background: 'var(--bg-card)',
                backdropFilter: 'blur(8px)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-lg)',
                boxShadow: 'var(--shadow-md)',
                overflow: 'hidden',
              }}
            >
              {/* Story image placeholder — aria-hidden */}
              <div
                aria-hidden="true"
                style={{
                  height: '160px',
                  background:
                    'linear-gradient(135deg, rgba(16,185,129,0.15) 0%, rgba(5,28,20,0.9) 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderBottom: '1px solid var(--border-color)',
                }}
              >
                <BookOpen
                  size={40}
                  style={{ color: 'var(--accent-primary)', opacity: 0.7 }}
                />
              </div>

              <div
                style={{
                  padding: '1.25rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.75rem',
                  flex: 1,
                }}
              >
                {/* Category badge */}
                <span
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    background: 'rgba(16,185,129,0.1)',
                    border: '1px solid rgba(16,185,129,0.2)',
                    borderRadius: 'var(--radius-full)',
                    padding: '0.2rem 0.65rem',
                    fontSize: '0.75rem',
                    fontWeight: 600,
                    color: 'var(--accent-primary)',
                    width: 'fit-content',
                  }}
                >
                  {story.category}
                </span>

                <h3
                  id={`story-title-${story.id}`}
                  style={{
                    fontSize: '1rem',
                    fontWeight: 700,
                    color: 'var(--text-primary)',
                    margin: 0,
                    lineHeight: 1.4,
                  }}
                >
                  {story.title}
                </h3>

                <p
                  style={{
                    fontSize: '0.875rem',
                    color: 'var(--text-secondary)',
                    margin: 0,
                    lineHeight: 1.6,
                    flex: 1,
                  }}
                >
                  {story.excerpt}
                </p>

                <Link
                  to={story.ctaPath}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.35rem',
                    fontSize: '0.875rem',
                    fontWeight: 600,
                    color: 'var(--accent-primary)',
                    textDecoration: 'none',
                    marginTop: '0.25rem',
                  }}
                  aria-label={`${story.ctaLabel} — ${story.title}`}
                >
                  {story.ctaLabel} <ArrowRight size={14} aria-hidden="true" />
                </Link>
              </div>
            </article>
          ))}
        </div>
      </Container>
    </section>
  );
};
