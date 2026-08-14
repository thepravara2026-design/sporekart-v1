import { FC } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useProducts, useCategories } from '../hooks/useCatalog';
import { ProductGrid } from '../components/ProductGrid';
import { CatalogFilterBar } from '../components/CatalogFilterBar';
import { PaginationControls } from '../components/PaginationControls';
import { ProductStatus } from '../../../types/catalog';

export const ProductListPage: FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '12', 10);
  const sort = searchParams.get('sort') || 'createdAt,desc';
  const categoryId = searchParams.get('categoryId') || '';
  const status = (searchParams.get('status') as ProductStatus) || undefined;
  const search = searchParams.get('search') || '';

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
    <div className="catalog-page" data-testid="product-list-page">
      <div className="page-header">
        <h1>Catalog Products</h1>
        <p className="page-subtitle">Browse our premium selection of mushroom cultures, extracts, and supplies.</p>
      </div>

      <CatalogFilterBar
        categories={categories}
        search={search}
        selectedCategory={categoryId}
        selectedStatus={status || ''}
        selectedSort={sort}
        onSearchChange={(val) => updateParam('search', val)}
        onCategoryChange={(val) => updateParam('categoryId', val)}
        onStatusChange={(val) => updateParam('status', val)}
        onSortChange={(val) => updateParam('sort', val)}
        onClearFilters={handleClearFilters}
      />

      {isLoading && (
        <div className="loading-container" data-testid="products-loading">
          <div className="skeleton-grid">
            {Array.from({ length: 6 }).map((_, idx) => (
              <div key={idx} className="skeleton-card" />
            ))}
          </div>
        </div>
      )}

      {isError && (
        <div className="alert alert-danger" data-testid="products-error">
          <h3>Unable to load catalog products</h3>
          <p>{error instanceof Error ? error.message : 'A network error occurred. Please verify backend connection.'}</p>
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => refetch()}>
            Retry
          </button>
        </div>
      )}

      {!isLoading && !isError && products.length === 0 && (
        <div className="empty-state" data-testid="products-empty">
          <h3>No products found</h3>
          <p>Try adjusting your search query or clearing filter criteria.</p>
          {(search || categoryId || status) && (
            <button type="button" className="btn btn-secondary btn-sm" onClick={handleClearFilters}>
              Clear All Filters
            </button>
          )}
        </div>
      )}

      {!isLoading && !isError && products.length > 0 && (
        <>
          <ProductGrid products={products} />
          {pageData && (
            <PaginationControls
              currentPage={pageData.page}
              totalPages={pageData.totalPages}
              totalElements={pageData.totalElements}
              pageSize={pageData.size}
              isFirst={pageData.first}
              isLast={pageData.last}
              onPageChange={handlePageChange}
            />
          )}
        </>
      )}
    </div>
  );
};
