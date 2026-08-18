import { FC } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useProduct } from '../hooks/useProduct';
import { ApiError } from '../../../services/apiError';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Skeleton } from '../../../components/ui/Skeleton';
import { ProductImage } from '../components/ProductImage';
import { ProductPrice } from '../components/ProductPrice';
import { ProductAvailability } from '../components/ProductAvailability';
import { CatalogErrorState } from '../components/CatalogErrorState';
import { ArrowLeft, ShoppingCart } from 'lucide-react';

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

  const isNotFound =
    isError &&
    error instanceof ApiError &&
    (error.status === 404 || error.code === 'CATALOG_PRODUCT_NOT_FOUND');

  return (
    <PageShell className="product-detail-page">
      <div data-testid="product-detail-page" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <Breadcrumb
          items={[
            { label: 'Home', path: '/' },
            { label: 'Products', path: '/products' },
            { label: product ? product.name : 'Product Details' },
          ]}
        />

        {isLoading && (
          <div className="loading-container" data-testid="product-detail-loading">
            <Card style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
              <Skeleton height="320px" borderRadius="var(--radius-lg)" />
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <Skeleton width="40%" height="24px" />
                <Skeleton width="80%" height="36px" />
                <Skeleton width="30%" height="32px" style={{ marginTop: '1rem' }} />
                <Skeleton width="100%" height="100px" style={{ marginTop: '1rem' }} />
              </div>
            </Card>
          </div>
        )}

        {isNotFound && (
          <div data-testid="product-not-found">
            <Card style={{ textAlign: 'center', padding: '3rem 2rem' }}>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
                Product Not Found
              </h2>
              <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
                The requested product with identifier &quot;{productId}&quot; could not be located in our catalog.
              </p>
              <Link to="/products" className="btn btn-primary btn-sm">
                Browse All Products
              </Link>
            </Card>
          </div>
        )}

        {isError && !isNotFound && (
          <div data-testid="product-detail-error">
            <CatalogErrorState
              message={error instanceof Error ? error.message : 'An error occurred while communicating with backend service.'}
              onRetry={() => refetch()}
            />
          </div>
        )}

        {!isLoading && !isError && product && (
          <Card data-testid="product-detail-card" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem', padding: '2rem' }}>
            <ProductImage alt={product.name} style={{ height: '340px' }} />

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <CardHeader style={{ padding: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
                  <ProductAvailability status={product.status} />
                  {product.category && (
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
                      Category: {product.category.name}
                    </span>
                  )}
                </div>

                <CardTitle style={{ fontSize: '1.85rem', lineHeight: 1.2 }}>{product.name}</CardTitle>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.25rem', fontFamily: 'var(--font-mono)' }}>
                  SKU: {product.sku}
                </div>
              </CardHeader>

              <CardContent style={{ padding: 0 }}>
                <ProductPrice price={product.price} currency={product.currency} style={{ fontSize: '1.75rem' }} />

                <div style={{ marginTop: '1.5rem' }}>
                  <h4 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
                    Description
                  </h4>
                  <p style={{ fontSize: '0.95rem', color: 'var(--text-secondary)', lineHeight: 1.6, margin: 0 }}>
                    {product.description || 'No detailed description available for this catalog item.'}
                  </p>
                </div>

                <div style={{ marginTop: '1.5rem', display: 'flex', gap: '2rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  <div>Added On: <strong style={{ color: 'var(--text-primary)' }}>{new Date(product.createdAt).toLocaleDateString()}</strong></div>
                  <div>Status: <strong style={{ color: 'var(--text-primary)' }}>{product.status}</strong></div>
                </div>
              </CardContent>

              <CardFooter style={{ padding: 0, marginTop: '1.5rem', justifyContent: 'flex-start', gap: '1rem' }}>
                <Link to="/products" className="btn btn-secondary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                  <ArrowLeft size={16} /> Back to Catalog
                </Link>

                <Button
                  variant={product.status === 'OUT_OF_STOCK' ? 'secondary' : 'primary'}
                  disabled={product.status === 'OUT_OF_STOCK'}
                  leftIcon={<ShoppingCart size={16} />}
                >
                  {product.status === 'OUT_OF_STOCK' ? 'Out of Stock' : 'Add to Cart'}
                </Button>
              </CardFooter>
            </div>
          </Card>
        )}
      </div>
    </PageShell>
  );
};
