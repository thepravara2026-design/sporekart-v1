import { FC } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useProduct } from '../hooks/useCatalog';
import { ApiError } from '../../../services/apiError';

export const ProductDetailPage: FC = () => {
  const { productId = '' } = useParams<{ productId: string }>();

  const {
    data: response,
    isLoading,
    isError,
    error,
    refetch,
  } = useProduct(productId);

  const product = response?.data;

  const isNotFound = isError && error instanceof ApiError && (error.status === 404 || error.code === 'CATALOG_PRODUCT_NOT_FOUND');

  const formatPrice = (price: number, currency: string) => {
    try {
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency || 'USD',
      }).format(price);
    } catch {
      return `$${price.toFixed(2)}`;
    }
  };

  return (
    <div className="product-detail-page" data-testid="product-detail-page">
      <div className="breadcrumb">
        <Link to="/products">&laquo; Back to Products</Link>
      </div>

      {isLoading && (
        <div className="loading-container" data-testid="product-detail-loading">
          <div className="skeleton-detail-card" />
        </div>
      )}

      {isNotFound && (
        <div className="alert alert-danger" data-testid="product-not-found">
          <h2>Product Not Found</h2>
          <p>The requested product with identifier &quot;{productId}&quot; could not be located in our catalog.</p>
          <Link to="/products" className="btn btn-primary btn-sm">
            Browse All Products
          </Link>
        </div>
      )}

      {isError && !isNotFound && (
        <div className="alert alert-danger" data-testid="product-detail-error">
          <h2>Unable to Load Product Details</h2>
          <p>{error instanceof Error ? error.message : 'An error occurred while communicating with backend service.'}</p>
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => refetch()}>
            Retry
          </button>
        </div>
      )}

      {!isLoading && !isError && product && (
        <div className="product-detail-card card" data-testid="product-detail-card">
          <div className="product-detail-header">
            <div>
              {product.category && (
                <span className="category-pill">{product.category.name}</span>
              )}
              <h1 className="product-detail-title">{product.name}</h1>
              <div className="product-detail-sku">SKU: {product.sku}</div>
            </div>
            <div className="product-detail-price">{formatPrice(product.price, product.currency)}</div>
          </div>

          <div className="product-detail-body">
            <h3>Description</h3>
            <p className="product-description">{product.description || 'No detailed description available for this catalog item.'}</p>
          </div>

          <div className="product-detail-meta">
            <div className="meta-item">
              <span className="meta-label">Status:</span> <strong>{product.status}</strong>
            </div>
            <div className="meta-item">
              <span className="meta-label">Added On:</span> {new Date(product.createdAt).toLocaleDateString()}
            </div>
          </div>

          <div className="product-detail-actions">
            <Link to="/products" className="btn btn-secondary">
              Back to Catalog
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};
