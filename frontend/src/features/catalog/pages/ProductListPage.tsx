import { FC } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useProducts } from '../hooks/useProducts';
import { useCategories } from '../hooks/useCategories';
import { ProductGrid } from '../components/ProductGrid';
import { ProductCardSkeleton } from '../components/ProductCardSkeleton';
import { CatalogFilterBar } from '../components/CatalogFilterBar';
import { CatalogPagination } from '../components/CatalogPagination';
import { CatalogEmptyState } from '../components/CatalogEmptyState';
import { CatalogErrorState } from '../components/CatalogErrorState';
import { PageShell } from '../../../components/layout/PageShell';
import { Grid } from '../../../components/layout/Grid';
import { ProductStatus } from '../../../types/catalog';

export const ProductListPage: FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '12', 10);
  const sort = searchParams.get('sort') || 'createdAt,desc';
  const categoryId = searchParams.get('categoryId') || '';
  const status = (searchParams.get('status') as ProductStatus) || undefined;
  const search = searchParams.get('search') || '';
  const minPriceStr = searchParams.get('minPrice') || '';
  const maxPriceStr = searchParams.get('maxPrice') || '';

  const minPrice = minPriceStr ? parseFloat(minPriceStr) : undefined;
  const maxPrice = maxPriceStr ? parseFloat(maxPriceStr) : undefined;

  const { data: categoriesResponse } = useCategories({ size: 50 });
  const categories = categoriesResponse?.data.content || [];

  const {
    data: productsResponse,
    isLoading,
    isError,
    error,
    refetch,
  } = useProducts({
    page,
    size,
    sort,
    categoryId: categoryId || undefined,
    status,
    search: search || undefined,
    minPrice,
    maxPrice,
  });

  const updateParam = (key: string, value: string) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (value) {
        next.set(key, value);
      } else {
        next.delete(key);
      }
      if (key !== 'page') {
        next.set('page', '0');
      }
      return next;
    });
  };

  const handlePageChange = (newPage: number) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('page', newPage.toString());
      return next;
    });
  };

  const handleClearFilters = () => {
    setSearchParams(new URLSearchParams());
  };

  const pageData = productsResponse?.data;
  const products = pageData?.content || [];

  return (
    <PageShell
      title="Catalog Products"
      subtitle="Browse our premium selection of high-yield mushroom cultures, spawn batches, and supplies."
      className="catalog-page"
    >
      <div data-testid="product-list-page">
        <CatalogFilterBar
          categories={categories}
          search={search}
          selectedCategory={categoryId}
          selectedStatus={status || ''}
          selectedSort={sort}
          minPrice={minPriceStr}
          maxPrice={maxPriceStr}
          onSearchChange={(val) => updateParam('search', val)}
          onCategoryChange={(val) => updateParam('categoryId', val)}
          onStatusChange={(val) => updateParam('status', val)}
          onSortChange={(val) => updateParam('sort', val)}
          onMinPriceChange={(val) => updateParam('minPrice', val)}
          onMaxPriceChange={(val) => updateParam('maxPrice', val)}
          onClearFilters={handleClearFilters}
        />

        {isLoading && (
          <div className="loading-container" data-testid="products-loading">
            <Grid minWidth="280px" gap="1.5rem">
              {Array.from({ length: 6 }).map((_, idx) => (
                <ProductCardSkeleton key={idx} />
              ))}
            </Grid>
          </div>
        )}

        {isError && (
          <div data-testid="products-error">
            <CatalogErrorState
              message={error instanceof Error ? error.message : 'A network error occurred. Please verify backend connection.'}
              onRetry={() => refetch()}
            />
          </div>
        )}

        {!isLoading && !isError && products.length === 0 && (
          <div data-testid="products-empty">
            <CatalogEmptyState onResetFilters={handleClearFilters} />
          </div>
        )}

        {!isLoading && !isError && products.length > 0 && (
          <>
            <ProductGrid products={products} />
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
