import { FC, useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { Container } from '../../../components/layout/Container';
import { FAQ_ITEMS } from '../data/marketingContent';

interface FAQItemProps {
  id: string;
  question: string;
  answer: string;
  isExpanded: boolean;
  onToggle: () => void;
}

const FAQAccordionItem: FC<FAQItemProps> = ({ id, question, answer, isExpanded, onToggle }) => {
  const headingId = `faq-heading-${id}`;
  const panelId = `faq-panel-${id}`;

  return (
    <div
      style={{
        borderBottom: '1px solid var(--border-color)',
      }}
    >
      <h3 style={{ margin: 0 }}>
        <button
          id={headingId}
          type="button"
          aria-expanded={isExpanded}
          aria-controls={panelId}
          onClick={onToggle}
          style={{
            width: '100%',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: '1rem',
            padding: '1.25rem 0',
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            textAlign: 'left',
            color: 'var(--text-primary)',
            fontSize: '1rem',
            fontWeight: 600,
            fontFamily: 'var(--font-family)',
          }}
        >
          <span>{question}</span>
          <ChevronDown
            size={18}
            aria-hidden="true"
            style={{
              color: 'var(--accent-primary)',
              flexShrink: 0,
              transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)',
              transition: 'transform var(--transition-fast)',
            }}
          />
        </button>
      </h3>

      <div
        id={panelId}
        role="region"
        aria-labelledby={headingId}
        hidden={!isExpanded}
        style={{
          paddingBottom: isExpanded ? '1.25rem' : 0,
        }}
      >
        <p
          style={{
            fontSize: '0.95rem',
            color: 'var(--text-secondary)',
            lineHeight: 1.7,
            margin: 0,
          }}
        >
          {answer}
        </p>
      </div>
    </div>
  );
};

export const FAQSection: FC = () => {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const handleToggle = (id: string) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  // Group FAQs by category
  const categories = Array.from(new Set(FAQ_ITEMS.map((f) => f.category)));

  return (
    <section
      aria-label="Frequently Asked Questions"
      style={{
        padding: 'clamp(3rem, 8vw, 6rem) 0',
        background: 'var(--bg-primary)',
      }}
    >
      <Container maxWidth="lg">
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
            Frequently Asked Questions
          </h2>
          <p
            style={{
              fontSize: '1rem',
              color: 'var(--text-secondary)',
              marginTop: '0.5rem',
            }}
          >
            Everything you need to know before your first order.
          </p>
        </div>

        {/* Accordion grouped by category */}
        {categories.map((category) => (
          <div
            key={category}
            style={{ marginBottom: '2.5rem' }}
          >
            <h3
              style={{
                fontSize: '0.8rem',
                fontWeight: 700,
                color: 'var(--accent-primary)',
                textTransform: 'uppercase',
                letterSpacing: '0.1em',
                margin: '0 0 0.25rem 0',
              }}
            >
              {category}
            </h3>
            <div
              style={{
                background: 'var(--bg-card)',
                backdropFilter: 'blur(8px)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-lg)',
                padding: '0 1.5rem',
              }}
            >
              {FAQ_ITEMS.filter((f) => f.category === category).map((item) => (
                <FAQAccordionItem
                  key={item.id}
                  id={item.id}
                  question={item.question}
                  answer={item.answer}
                  isExpanded={expandedId === item.id}
                  onToggle={() => handleToggle(item.id)}
                />
              ))}
            </div>
          </div>
        ))}
      </Container>
    </section>
  );
};
