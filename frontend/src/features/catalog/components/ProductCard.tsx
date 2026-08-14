import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Product } from '../../../types/catalog';

interface ProductCardProps {
  product: Product;
}

export const ProductCard: FC<ProductCardProps> = ({ product }) => {
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

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <span className="badge badge-success">In Stock</span>;
      case 'OUT_OF_STOCK':
        return <span className="badge badge-danger">Out of Stock</span>;
      default:
        return <span className="badge" style={{ background: 'rgba(156, 163, 175, 0.2)', color: '#9ca3af' }}>{status}</span>;
    }
  };

  return (
    <div className="product-card" data-testid={`product-card-${product.id}`}>
      <div className="product-card-header">
        {product.category && (
          <span className="category-pill">{product.category.name}</span>
        )}
        {getStatusBadge(product.status)}
      </div>

      <h3 className="product-title">
        <Link to={`/products/${product.id}`}>{product.name}</Link>
      </h3>

      <div className="product-sku">SKU: {product.sku}</div>

      {product.description && (
        <p className="product-description">{product.description}</p>
      )}

      <div className="product-card-footer">
        <div className="product-price">{formatPrice(product.price, product.currency)}</div>
        <Link to={`/products/${product.id}`} className="btn btn-primary btn-sm">
          View Details
        </Link>
      </div>
    </div>
  );
};
