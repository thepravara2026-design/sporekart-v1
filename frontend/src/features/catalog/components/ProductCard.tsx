import { FC, useState } from 'react';
import { Link } from 'react-router-dom';
import { Product, ProductVariant } from '../types/catalog';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { ProductImage } from './ProductImage';
import { ProductPrice } from './ProductPrice';
import { ProductAvailability } from './ProductAvailability';
import { ShoppingCart, Eye } from 'lucide-react';

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

  return (
    <Card className={`product-card ${className}`} style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Link to={`/products/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
        <ProductImage alt={product.name} />
      </Link>

      <CardHeader style={{ marginTop: '0.75rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <ProductAvailability status={currentStatus} availableStock={currentStock} />
          {product.category && (
            <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              {product.category.name}
            </span>
          )}
        </div>
        <Link to={`/products/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
          <CardTitle style={{ fontSize: '1.1rem', marginTop: '0.5rem', lineHeight: 1.3 }}>
            {product.name}
          </CardTitle>
        </Link>
        <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem', fontFamily: 'var(--font-mono)' }}>
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
