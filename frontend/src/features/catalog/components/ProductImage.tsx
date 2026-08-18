import { FC, useState } from 'react';
import { Sprout } from 'lucide-react';

export interface ProductImageProps {
  src?: string | null;
  alt: string;
  className?: string;
  style?: React.CSSProperties;
}

export const ProductImage: FC<ProductImageProps> = ({ src, alt, className = '', style = {} }) => {
  const [hasError, setHasError] = useState(false);

  return (
    <div
      className={`product-image-container ${className}`}
      style={{
        width: '100%',
        aspectRatio: '4 / 3',
        backgroundColor: 'rgba(5, 28, 20, 0.6)',
        borderRadius: 'var(--radius-md)',
        overflow: 'hidden',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        ...style,
      }}
    >
      {src && !hasError ? (
        <img
          src={src}
          alt={alt}
          onError={() => setHasError(true)}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem', color: 'var(--accent-primary)' }}>
          <Sprout size={36} />
          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Mushroom Spawn</span>
        </div>
      )}
    </div>
  );
};
