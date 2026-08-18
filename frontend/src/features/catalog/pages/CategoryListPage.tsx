import { FC } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useCategories } from '../hooks/useCategories';
import { CategoryGrid } from '../components/CategoryGrid';
import { CatalogSearch } from '../components/CatalogSearch';
import { CatalogPagination } from '../components/CatalogPagination';
import { CatalogEmptyState } from '../components/CatalogEmptyState';
import { CatalogErrorState } from '../components/CatalogErrorState';
import { PageShell } from '../../../components/layout/PageShell';
import { Grid } from '../../../components/layout/Grid';
import { Skeleton } from '../../../components/ui/Skeleton';
import { Card } from '../../../components/ui/Card';

export const CategoryListPage: FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '12', 10);
  const sort = searchParams.get('sort') || 'name,asc';
  const search = searchParams.get('search') || '';

  const {
    data: categoriesResponse,
    isLoading,
    isError,
    error,
    refetch,
  } = useCategories({
    page,
    size,
    sort,
    search: search || undefined,
  });

  const handlePageChange = (newPage: number) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('page', newPage.toString());
      return next;
    });
  };

  const handleSearchChange = (value: string) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (value) {
        next.set('search', value);
      } else {
        next.delete('search');
      }
      next.set('page', '0');
      return next;
    });
  };

  const pageData = categoriesResponse?.data;
  const categories = pageData?.content || [];

  return (
    <PageShell
      title="Catalog Categories"
      subtitle="Explore mushroom product categories, cultivation tiers, and spawn classifications."
      className="catalog-page"
    >
      <div data-testid="category-list-page" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <div style={{ maxWidth: '400px' }}>
          <CatalogSearch
            value={search}
            onSearch={handleSearchChange}
            placeholder="Search categories..."
          />
        </div>

        {isLoading && (
          <div className="loading-container" data-testid="categories-loading">
            <Grid minWidth="260px" gap="1.5rem">
              {Array.from({ length: 4 }).map((_, idx) => (
                <Card key={idx}>
                  <Skeleton height="24px" width="60%" />
                  <Skeleton height="16px" width="40%" style={{ marginTop: '0.5rem' }} />
                  <Skeleton height="40px" style={{ marginTop: '1rem' }} />
                </Card>
              ))}
            </Grid>
          </div>
        )}

        {isError && (
          <div data-testid="categories-error">
            <CatalogErrorState
              message={error instanceof Error ? error.message : 'A network error occurred.'}
              onRetry={() => refetch()}
            />
          </div>
        )}

        {!isLoading && !isError && categories.length === 0 && (
          <div data-testid="categories-empty">
            <CatalogEmptyState
              title="No Categories Found"
              description="Try searching with a different category name."
            />
          </div>
        )}

        {!isLoading && !isError && categories.length > 0 && (
          <>
            <div data-testid="category-grid">
              <CategoryGrid categories={categories} />
            </div>

            {pageData && pageData.totalPages > 1 && (
              <CatalogPagination
                currentPage={pageData.page}
                totalPages={pageData.totalPages}
                onPageChange={handlePageChange}
              />
            )}
          </>
        )}
      </div>
    </PageShell>
  );
};
