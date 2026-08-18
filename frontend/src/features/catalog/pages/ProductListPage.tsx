import { FC, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { SlidersHorizontal } from 'lucide-react';
import { useProducts } from '../hooks/useProducts';
import { useCategories } from '../hooks/useCategories';
import { ProductGrid } from '../components/ProductGrid';
import { ProductCardSkeleton } from '../components/ProductCardSkeleton';
import { CatalogToolbar, ActiveFilterChip } from '../components/CatalogToolbar';
import { CatalogFiltersDrawer } from '../components/CatalogFiltersDrawer';
import { CatalogPagination } from '../components/CatalogPagination';
import { CatalogEmptyState } from '../components/CatalogEmptyState';
import { CatalogErrorState } from '../components/CatalogErrorState';
import { PageShell } from '../../../components/layout/PageShell';
import { Grid } from '../../../components/layout/Grid';
import { ProductStatus } from '../../../types/catalog';

export const ProductListPage: FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [drawerOpen, setDrawerOpen] = useState(false);

  // ─── URL-driven state (single source of truth) ───────────────────────────
  const page = Math.max(0, parseInt(searchParams.get('page') || '0', 10));
  const size = Math.max(1, parseInt(searchParams.get('size') || '12', 10));
  const sort = searchParams.get('sort') || 'createdAt,desc';
  const categoryId = searchParams.get('categoryId') || '';
  const status = (searchParams.get('status') as ProductStatus) || undefined;
  const search = searchParams.get('search') || '';
  const minPriceStr = searchParams.get('minPrice') || '';
  const maxPriceStr = searchParams.get('maxPrice') || '';
  const minPrice = minPriceStr ? parseFloat(minPriceStr) : undefined;
  const maxPrice = maxPriceStr ? parseFloat(maxPriceStr) : undefined;

  // ─── Data fetching ────────────────────────────────────────────────────────
  const { data: categoriesResponse } = useCategories({ size: 100 });
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

  const pageData = productsResponse?.data;
  const products = pageData?.content || [];

  // ─── URL mutation helpers ─────────────────────────────────────────────────
  const updateParam = useCallback(
    (key: string, value: string) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (value) {
          next.set(key, value);
        } else {
          next.delete(key);
        }
        if (key !== 'page') next.set('page', '0');
        return next;
      });
    },
    [setSearchParams],
  );

  const handlePageChange = useCallback(
    (newPage: number) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        next.set('page', newPage.toString());
        return next;
      });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },
    [setSearchParams],
  );

  const handleClearFilters = useCallback(() => {
    setSearchParams(new URLSearchParams());
  }, [setSearchParams]);

  // ─── Active filter chips ──────────────────────────────────────────────────
  const activeFilters: ActiveFilterChip[] = [];

  if (search) {
    activeFilters.push({ label: `Search: "${search}"`, onRemove: () => updateParam('search', '') });
  }
  if (categoryId) {
    const cat = categories.find((c) => c.id === categoryId);
    activeFilters.push({
      label: `Category: ${cat?.name ?? categoryId}`,
      onRemove: () => updateParam('categoryId', ''),
    });
  }
  if (status) {
    const label = status === 'ACTIVE' ? 'In Stock' : status === 'OUT_OF_STOCK' ? 'Out of Stock' : status;
    activeFilters.push({ label: `Status: ${label}`, onRemove: () => updateParam('status', '') });
  }
  if (minPriceStr) {
    activeFilters.push({ label: `Min: ₹${minPriceStr}`, onRemove: () => updateParam('minPrice', '') });
  }
  if (maxPriceStr) {
    activeFilters.push({ label: `Max: ₹${maxPriceStr}`, onRemove: () => updateParam('maxPrice', '') });
  }

  const hasActiveFilters = activeFilters.length > 0;

  // ─── Mobile filter trigger button ────────────────────────────────────────
  const filterTrigger = (
    <button
      type="button"
      aria-label={`Open filters${hasActiveFilters ? ` (${activeFilters.length} active)` : ''}`}
      aria-expanded={drawerOpen}
      onClick={() => setDrawerOpen(true)}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.4rem',
        height: '2.5rem',
        padding: '0 0.875rem',
        background: hasActiveFilters ? 'rgba(16,185,129,0.12)' : 'var(--surface-elevated)',
        border: `1px solid ${hasActiveFilters ? 'rgba(16,185,129,0.4)' : 'var(--border-primary)'}`,
        borderRadius: 'var(--radius-md)',
        color: hasActiveFilters ? 'var(--accent-primary)' : 'var(--text-secondary)',
        cursor: 'pointer',
        fontSize: '0.875rem',
        fontWeight: 600,
        whiteSpace: 'nowrap',
      }}
    >
      <SlidersHorizontal size={15} aria-hidden="true" />
      Filters
      {hasActiveFilters && (
        <span
          style={{
            background: 'var(--accent-primary)',
            color: '#fff',
            borderRadius: 'var(--radius-full)',
            fontSize: '0.7rem',
            fontWeight: 700,
            padding: '0.05rem 0.4rem',
            lineHeight: 1.4,
          }}
          aria-hidden="true"
        >
          {activeFilters.length}
        </span>
      )}
    </button>
  );

  return (
    <PageShell
      title="Product Catalog"
      subtitle="Browse premium mushroom spawn, cultures, substrate, and cultivation supplies."
      className="catalog-page"
    >
      <div data-testid="product-list-page">
        {/* Toolbar: search, sort, filter trigger, active chips */}
        <CatalogToolbar
          search={search}
          onSearch={(val) => updateParam('search', val)}
          sort={sort}
          onSort={(val) => updateParam('sort', val)}
          totalElements={isLoading ? undefined : pageData?.totalElements}
          isLoading={isLoading}
          filterTrigger={filterTrigger}
          activeFilters={activeFilters}
        />

        {/* Mobile/desktop filter drawer */}
        <CatalogFiltersDrawer
          isOpen={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          categories={categories}
          selectedCategory={categoryId}
          selectedStatus={status || ''}
          minPrice={minPriceStr}
          maxPrice={maxPriceStr}
          onCategoryChange={(val) => updateParam('categoryId', val)}
          onStatusChange={(val) => updateParam('status', val)}
          onMinPriceChange={(val) => updateParam('minPrice', val)}
          onMaxPriceChange={(val) => updateParam('maxPrice', val)}
          onClearFilters={handleClearFilters}
          hasActiveFilters={hasActiveFilters}
        />

        {/* Loading state */}
        {isLoading && (
          <div className="loading-container" data-testid="products-loading">
            <Grid minWidth="280px" gap="1.5rem">
              {Array.from({ length: size }).map((_, idx) => (
                <ProductCardSkeleton key={idx} />
              ))}
            </Grid>
          </div>
        )}

        {/* Error state */}
        {isError && !isLoading && (
          <div data-testid="products-error">
            <CatalogErrorState
              message={
                error instanceof Error
                  ? error.message
                  : 'A network error occurred. Please verify backend connection.'
              }
              onRetry={() => refetch()}
            />
          </div>
        )}

        {/* Empty state */}
        {!isLoading && !isError && products.length === 0 && (
          <div data-testid="products-empty">
            <CatalogEmptyState
              title={search ? `No products found for "${search}"` : 'No products found'}
              description={
                hasActiveFilters
                  ? 'Try adjusting your filters or clearing them to see more products.'
                  : 'The catalog is currently empty. Check back soon.'
              }
              onResetFilters={hasActiveFilters ? handleClearFilters : undefined}
            />
          </div>
        )}

        {/* Product grid */}
        {!isLoading && !isError && products.length > 0 && (
          <>
            <div data-testid="products-grid">
              <ProductGrid products={products} />
            </div>

            {/* Pagination */}
            {pageData && pageData.totalPages > 1 && (
              <div style={{ marginTop: '2rem' }}>
                <CatalogPagination
                  currentPage={pageData.page}
                  totalPages={pageData.totalPages}
                  onPageChange={handlePageChange}
                />
              </div>
            )}
          </>
        )}
      </div>
    </PageShell>
  );
};
