import { FC } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { useCategories } from '../../catalog/hooks/useCategories';
import { CategoryCard } from '../../catalog/components/CategoryCard';
import { Skeleton } from '../../../components/ui/Skeleton';
import { EmptyState } from '../../../components/ui/EmptyState';
import { CatalogErrorState } from '../../catalog/components/CatalogErrorState';
import { Container } from '../../../components/layout/Container';

export const CategorySection: FC = () => {
  const { data: response, isLoading, isError, error, refetch } = useCategories({
    size: 8,
    sort: 'name,asc',
  });

  const categories = response?.data?.content ?? [];

  return (
    <section
      aria-label="Shop by Category"
      style={{
        padding: 'clamp(3rem, 8vw, 6rem) 0',
        background: 'var(--bg-primary)',
      }}
    >
      <Container>
        {/* Section header */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'flex-end',
            flexWrap: 'wrap',
            gap: '1rem',
            marginBottom: '2.5rem',
          }}
        >
          <div>
            <h2
              style={{
                fontSize: 'clamp(1.5rem, 4vw, 2.25rem)',
                fontWeight: 800,
                color: 'var(--text-primary)',
                margin: 0,
                lineHeight: 1.2,
              }}
            >
              Shop by Category
            </h2>
            <p style={{ fontSize: '1rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
              Find the right spawn or substrate for your cultivation setup.
            </p>
          </div>
          <Link
            to="/categories"
            className="btn btn-secondary btn-sm"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', whiteSpace: 'nowrap' }}
          >
            All Categories <ArrowRight size={15} aria-hidden="true" />
          </Link>
        </div>

        {/* Loading */}
        {isLoading && (
          <div
            data-testid="category-section-loading"
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
              gap: '1.25rem',
            }}
          >
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} height="120px" borderRadius="var(--radius-lg)" />
            ))}
          </div>
        )}

        {/* Error */}
        {isError && !isLoading && (
          <CatalogErrorState
            title="Unable to Load Categories"
            message={error instanceof Error ? error.message : 'Could not load category data.'}
            onRetry={() => refetch()}
          />
        )}

        {/* Empty */}
        {!isLoading && !isError && categories.length === 0 && (
          <EmptyState
            title="No Categories Yet"
            description="Catalog categories will appear here when available."
          />
        )}

        {/* Success */}
        {!isLoading && !isError && categories.length > 0 && (
          <div
            data-testid="category-section-grid"
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
              gap: '1.25rem',
            }}
          >
            {categories.map((category) => (
              <CategoryCard key={category.id} category={category} />
            ))}
          </div>
        )}
      </Container>
    </section>
  );
};
