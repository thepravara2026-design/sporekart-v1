import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Product } from '../types/catalog';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { ProductImage } from './ProductImage';
import { ProductPrice } from './ProductPrice';
import { ProductAvailability } from './ProductAvailability';
import { ShoppingCart, Eye } from 'lucide-react';

export interface ProductCardProps {
  product: Product;
  onAddToCart?: (product: Product) => void;
  className?: string;
}

export const ProductCard: FC<ProductCardProps> = ({ product, onAddToCart, className = '' }) => {
  const isOutOfStock = product.status === 'OUT_OF_STOCK' || product.status === 'DISCONTINUED';

  return (
    <Card className={`product-card ${className}`} style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Link to={`/products/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
        <ProductImage alt={product.name} />
      </Link>

      <CardHeader style={{ marginTop: '0.75rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <ProductAvailability status={product.status} />
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
          SKU: {product.sku}
        </div>
      </CardHeader>

      <CardContent style={{ flex: 1 }}>
        {product.description && (
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '0 0 1rem 0', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
            {product.description}
          </p>
        )}
        <ProductPrice price={product.price} strikeOutPrice={product.strikeOutPrice} currency={product.currency} showDiscountBadge={true} />
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
          onClick={() => onAddToCart?.(product)}
        >
          {isOutOfStock ? 'Out of Stock' : 'Add to Cart'}
        </Button>
      </CardFooter>
    </Card>
  );
};
