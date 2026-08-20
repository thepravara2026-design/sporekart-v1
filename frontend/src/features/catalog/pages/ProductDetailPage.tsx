import { FC, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useProduct } from '../hooks/useProduct';
import { useAddToCart } from '../hooks/useAddToCart';
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
import { ProductStickyAction, useStickyActionVisibility } from '../components/ProductStickyAction';
import { isProductPurchasable } from '../utils/catalogUtils';
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
  const variants = product?.variants || [];

  const [quantity, setQuantity] = useState(1);
  const [selectedVariantId, setSelectedVariantId] = useState<string | null>(
    variants.length > 0 ? variants[0].id : null
  );
  const addToCartMutation = useAddToCart();
  const purchasePanelRef = useRef<HTMLDivElement>(null);
  const stickyVisible = useStickyActionVisibility(purchasePanelRef);

  const isNotFound =
    isError &&
    error instanceof ApiError &&
    (error.status === 404 || error.code === 'CATALOG_PRODUCT_NOT_FOUND');

  const purchasable = product ? isProductPurchasable(product.status) : false;

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

  const handleStickyAddToCart = () => {
    if (!product || !purchasable || addToCartMutation.isPending) return;
    addToCartMutation.mutate({ productId: product.id, variantId: selectedVariantId, quantity });
  };

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
          <div style={{ paddingBottom: stickyVisible ? '5.5rem' : 0, transition: 'padding-bottom var(--transition-fast)' }}>
            <Card
              ref={purchasePanelRef}
              data-testid="product-detail-card"
              className="product-detail-layout"
            >
              <ProductGallery
                images={(product.images || []).map((img) => img.imageUrl).filter(Boolean)}
                productName={product.name}
              />
              <ProductInfo
                product={product}
                quantity={quantity}
                onQuantityChange={setQuantity}
                selectedVariantId={selectedVariantId}
                onSelectedVariantChange={setSelectedVariantId}
                mutation={addToCartMutation}
              />
            </Card>

            <ProductDescription description={product.description} />

            <RelatedProducts categoryId={product.category?.id} currentProductId={product.id} />

            <div style={{ marginTop: '1.5rem' }}>
              <Link to="/products" className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                <ArrowLeft size={16} /> Back to Products Catalog
              </Link>
            </div>
          </div>
        )}

        {product && (
          <ProductStickyAction
            productName={product.name}
            visible={stickyVisible && purchasable}
            isPending={addToCartMutation.isPending}
            disabled={!purchasable}
            onAddToCart={handleStickyAddToCart}
          />
        )}
      </div>
    </PageShell>
  );
};