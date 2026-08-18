import { FC } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useProduct } from '../hooks/useProduct';
import { ApiError } from '../../../services/apiError';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb, BreadcrumbItem } from '../../../components/ui/Breadcrumb';
import { Card } from '../../../components/ui/Card';
import { ProductGallery } from '../components/ProductGallery';
import { ProductInfo } from '../components/ProductInfo';
import { ProductDescription } from '../components/ProductDescription';
import { RelatedProducts } from '../components/RelatedProducts';
import { ProductDetailSkeleton } from '../components/ProductDetailSkeleton';
import { CatalogErrorState } from '../components/CatalogErrorState';
import { ArrowLeft } from 'lucide-react';

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

  const breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Home', path: '/' },
    { label: 'Products', path: '/products' },
  ];

  if (product?.category) {
    breadcrumbItems.push({
      label: product.category.name,
      path: `/products?categoryId=${product.category.id}`,
    });
  }

  if (product) {
    breadcrumbItems.push({ label: product.name });
  } else {
    breadcrumbItems.push({ label: 'Product Details' });
  }

  return (
    <PageShell className="product-detail-page">
      <div data-testid="product-detail-page" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <Breadcrumb items={breadcrumbItems} />

        {isLoading && (
          <div className="loading-container" data-testid="product-detail-loading">
            <ProductDetailSkeleton />
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
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                <Link to="/products" className="btn btn-primary btn-sm">
                  Browse All Products
                </Link>
              </div>
            </Card>
          </div>
        )}

        {isError && !isNotFound && (
          <div data-testid="product-detail-error">
            <CatalogErrorState
              title="Unable to Load Product Details"
              message={error instanceof Error ? error.message : 'An error occurred while communicating with backend service.'}
              onRetry={() => refetch()}
            />
          </div>
        )}

        {!isLoading && !isError && product && (
          <>
            <Card data-testid="product-detail-card" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2.5rem', padding: '2rem' }}>
              <ProductGallery productName={product.name} />
              <ProductInfo product={product} />
            </Card>

            <ProductDescription description={product.description} />

            <RelatedProducts categoryId={product.category?.id} currentProductId={product.id} />

            <div style={{ marginTop: '1.5rem' }}>
              <Link to="/products" className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                <ArrowLeft size={16} /> Back to Products Catalog
              </Link>
            </div>
          </>
        )}
      </div>
    </PageShell>
  );
};
