import { FC } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { useProducts } from '../../catalog/hooks/useProducts';
import { ProductCard } from '../../catalog/components/ProductCard';
import { ProductCardSkeleton } from '../../catalog/components/ProductCardSkeleton';
import { CatalogErrorState } from '../../catalog/components/CatalogErrorState';
import { EmptyState } from '../../../components/ui/EmptyState';
import { Container } from '../../../components/layout/Container';

const FEATURED_COUNT = 6;

/**
 * FeaturedProducts
 *
 * Data source: existing catalogApi via useProducts().
 * No dedicated "featured" endpoint exists — we display the first page of
 * products sorted by name as the catalog landing set.
 * When a backend-featured flag or sort field is added, update the query params.
 */
export const FeaturedProducts: FC = () => {
  const { data: response, isLoading, isError, error, refetch } = useProducts({
    size: FEATURED_COUNT,
    sort: 'name,asc',
  });

  const products = response?.data?.content ?? [];

  return (
    <section
      aria-label="Featured Products"
      style={{
        padding: 'clamp(3rem, 8vw, 6rem) 0',
        background: 'var(--bg-secondary)',
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
              Shop the Catalog
            </h2>
            <p
              style={{
                fontSize: '1rem',
                color: 'var(--text-secondary)',
                marginTop: '0.4rem',
              }}
            >
              Fresh stock, sterile batches, ready to fruit.
            </p>
          </div>
          <Link
            to="/products"
            className="btn btn-secondary btn-sm"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.4rem',
              whiteSpace: 'nowrap',
            }}
          >
            View All Products <ArrowRight size={15} aria-hidden="true" />
          </Link>
        </div>

        {/* Loading */}
        {isLoading && (
          <div
            data-testid="featured-products-loading"
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
              gap: '1.5rem',
            }}
          >
            {Array.from({ length: FEATURED_COUNT }).map((_, i) => (
              <ProductCardSkeleton key={i} />
            ))}
          </div>
        )}

        {/* Error */}
        {isError && !isLoading && (
          <CatalogErrorState
            title="Unable to Load Products"
            message={error instanceof Error ? error.message : 'Could not connect to the catalog service.'}
            onRetry={() => refetch()}
          />
        )}

        {/* Empty */}
        {!isLoading && !isError && products.length === 0 && (
          <EmptyState
            title="No Products Available"
            description="The catalog is currently empty. Check back soon for new spawn and substrate listings."
            action={
              <Link to="/categories" className="btn btn-primary btn-sm">
                Browse Categories
              </Link>
            }
          />
        )}

        {/* Success: product grid */}
        {!isLoading && !isError && products.length > 0 && (
          <>
            <div
              data-testid="featured-products-grid"
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
                gap: '1.5rem',
              }}
            >
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            <div style={{ textAlign: 'center', marginTop: '3rem' }}>
              <Link
                to="/products"
                className="btn btn-primary"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
              >
                View All Products <ArrowRight size={18} aria-hidden="true" />
              </Link>
            </div>
          </>
        )}
      </Container>
    </section>
  );
};
