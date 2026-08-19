import { FC, useState } from 'react';
import { Link } from 'react-router-dom';
import { Product, ProductVariant } from '../types/catalog';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { ProductImageCarousel } from './ProductImageCarousel';
import { ProductPrice } from './ProductPrice';
import { ProductAvailability } from './ProductAvailability';
import { MAX_PRODUCT_IMAGES } from '../constants/catalogConstants';
import { ShoppingCart, Eye, Tag } from 'lucide-react';

export interface ProductCardProps {
  product: Product;
  onAddToCart?: (product: Product, selectedVariant?: ProductVariant | null) => void;
  className?: string;
}

export const ProductCard: FC<ProductCardProps> = ({ product, onAddToCart, className = '' }) => {
  const variants = product.variants || [];
  const [selectedVariantId, setSelectedVariantId] = useState<string | null>(
    variants.length > 0 ? variants[0].id : null
  );

  const selectedVariant = variants.find((v) => v.id === selectedVariantId) || (variants.length > 0 ? variants[0] : null);

  const currentPrice = selectedVariant ? selectedVariant.sellingPrice : product.price;
  const currentStrikeOutPrice = selectedVariant ? selectedVariant.strikeOutPrice : product.strikeOutPrice;
  const currentStatus = selectedVariant ? selectedVariant.status : product.status;
  const currentStock = selectedVariant && typeof selectedVariant.availableQuantity === 'number'
    ? selectedVariant.availableQuantity
    : undefined;
  const isOutOfStock = currentStatus === 'OUT_OF_STOCK' || product.status === 'OUT_OF_STOCK' || product.status === 'DISCONTINUED' || (typeof currentStock === 'number' && currentStock <= 0);

  const carouselImages = [
    ...(product.images || []).map((img) => img.imageUrl),
    ...(product.imageUrl ? [product.imageUrl] : []),
  ]
    .filter(Boolean)
    .filter((url, index, arr) => arr.indexOf(url) === index)
    .slice(0, MAX_PRODUCT_IMAGES);

  return (
    <Card className={`product-card ${className}`} style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Link to={`/products/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
        <ProductImageCarousel images={carouselImages} alt={product.name} />
      </Link>

      <CardHeader style={{ marginTop: '0.75rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <ProductAvailability status={currentStatus} availableStock={currentStock} />
          {product.category && (
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.3rem',
                fontSize: '0.72rem',
                color: 'var(--accent-primary)',
                fontWeight: 600,
                padding: '0.2rem 0.6rem',
                borderRadius: 'var(--radius-full)',
                background: 'rgba(16, 185, 129, 0.08)',
                border: '1px solid rgba(16, 185, 129, 0.25)',
              }}
            >
              <Tag size={11} aria-hidden="true" />
              {product.category.name}
            </span>
          )}
        </div>
        <Link to={`/products/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
          <CardTitle style={{ fontSize: '1.1rem', marginTop: '0.5rem', lineHeight: 1.3 }}>
            {product.name}
          </CardTitle>
        </Link>
        <div
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            fontSize: '0.7rem',
            color: 'var(--text-secondary)',
            marginTop: '0.2rem',
            fontFamily: 'var(--font-mono)',
            background: 'rgba(255, 255, 255, 0.05)',
            border: '1px solid var(--border-subtle)',
            padding: '0.15rem 0.55rem',
            borderRadius: 'var(--radius-full)',
            letterSpacing: '0.02em',
          }}
        >
          SKU: {selectedVariant ? selectedVariant.sku : product.sku}
        </div>
      </CardHeader>

      <CardContent style={{ flex: 1 }}>
        {product.description && (
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '0 0 0.75rem 0', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
            {product.description}
          </p>
        )}

        {variants.length > 1 && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem', marginBottom: '0.75rem' }} role="group" aria-label="Product size options">
            {variants.map((v) => {
              const isSelected = selectedVariant?.id === v.id;
              return (
                <button
                  key={v.id}
                  type="button"
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    setSelectedVariantId(v.id);
                  }}
                  aria-pressed={isSelected}
                  style={{
                    fontSize: '0.75rem',
                    fontWeight: isSelected ? 700 : 500,
                    padding: '0.2rem 0.55rem',
                    borderRadius: '9999px',
                    border: isSelected ? '1.5px solid var(--accent-primary, #10b981)' : '1px solid rgba(255, 255, 255, 0.15)',
                    backgroundColor: isSelected ? 'rgba(16, 185, 129, 0.15)' : 'rgba(255, 255, 255, 0.05)',
                    color: isSelected ? 'var(--accent-primary, #10b981)' : 'var(--text-secondary, #9ca3af)',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease-in-out',
                    boxShadow: isSelected ? '0 0 8px 0 rgba(16, 185, 129, 0.25)' : 'none',
                  }}
                  onMouseEnter={(e) => {
                    if (!isSelected) {
                      e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (!isSelected) {
                      e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.05)';
                    }
                  }}
                >
                  {v.formattedQuantity}
                </button>
              );
            })}
          </div>
        )}

        <ProductPrice price={currentPrice} strikeOutPrice={currentStrikeOutPrice} currency={product.currency} showDiscountBadge={true} />
      </CardContent>

      <CardFooter style={{ marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid rgba(255, 255, 255, 0.08)', gap: '0.5rem' }}>
        <Link
          to={`/products/${product.id}`}
          className="btn btn-secondary btn-sm"
          style={{ flex: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.35rem' }}
        >
          <Eye size={14} /> View Details
        </Link>
        <Button
          variant={isOutOfStock ? 'secondary' : 'primary'}
          size="sm"
          disabled={isOutOfStock}
          style={{ flex: 1 }}
          leftIcon={<ShoppingCart size={14} />}
          onClick={() => {
            if (selectedVariant) {
              onAddToCart?.(product, selectedVariant);
            } else {
              onAddToCart?.(product);
            }
          }}
        >
          {isOutOfStock ? 'Out of Stock' : 'Add to Cart'}
        </Button>
      </CardFooter>
    </Card>
  );
};
