import { FC } from 'react';

export interface ProductDescriptionProps {
  description?: string | null;
  className?: string;
}

export const ProductDescription: FC<ProductDescriptionProps> = ({
  description,
  className = '',
}) => {
  return (
    <div className={`product-description-panel card ${className}`} style={{ padding: '1.5rem', marginTop: '2rem' }}>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.75rem' }}>
        Product Description
      </h3>
      <p style={{ fontSize: '0.95rem', color: 'var(--text-secondary)', lineHeight: 1.6, margin: 0, whiteSpace: 'pre-line' }}>
        {description || 'No detailed cultivation description available for this catalog item.'}
      </p>
    </div>
  );
};
