import { FC } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useCategories } from '../hooks/useCatalog';
import { PaginationControls } from '../components/PaginationControls';

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
    <div className="catalog-page" data-testid="category-list-page">
      <div className="page-header">
        <h1>Catalog Categories</h1>
        <p className="page-subtitle">Explore mushroom product categories and classifications.</p>
      </div>

      <div className="catalog-filter-bar">
        <div className="filter-group filter-search">
          <label htmlFor="category-search" className="filter-label">Search Categories</label>
          <input
            id="category-search"
            type="text"
            className="form-control"
            placeholder="Search categories..."
            value={search}
            onChange={(e) => handleSearchChange(e.target.value)}
          />
        </div>
      </div>

      {isLoading && (
        <div className="loading-container" data-testid="categories-loading">
          <div className="skeleton-grid">
            {Array.from({ length: 4 }).map((_, idx) => (
              <div key={idx} className="skeleton-card" />
            ))}
          </div>
        </div>
      )}

      {isError && (
        <div className="alert alert-danger" data-testid="categories-error">
          <h3>Unable to load categories</h3>
          <p>{error instanceof Error ? error.message : 'A network error occurred.'}</p>
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => refetch()}>
            Retry
          </button>
        </div>
      )}

      {!isLoading && !isError && categories.length === 0 && (
        <div className="empty-state" data-testid="categories-empty">
          <h3>No categories found</h3>
          <p>Try searching with a different term.</p>
        </div>
      )}

      {!isLoading && !isError && categories.length > 0 && (
        <>
          <div className="category-grid" data-testid="category-grid">
            {categories.map((cat) => (
              <div key={cat.id} className="category-card card">
                <h3>{cat.name}</h3>
                <div className="category-slug">Slug: {cat.slug}</div>
                {cat.description && <p>{cat.description}</p>}
                <div className="category-card-footer">
                  <Link to={`/products?categoryId=${cat.id}`} className="btn btn-primary btn-sm">
                    View Category Products
                  </Link>
                </div>
              </div>
            ))}
          </div>

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
