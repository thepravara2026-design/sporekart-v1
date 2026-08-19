import { FC, useState } from 'react';
import { Sprout } from 'lucide-react';

export interface ProductImageProps {
  src?: string | null;
  alt: string;
  /** Use 'eager' for above-the-fold images (e.g. product detail hero). Defaults to 'lazy'. */
  loading?: 'eager' | 'lazy';
  className?: string;
  style?: React.CSSProperties;
  /** Invoked when the image fails to load and the fallback is shown. */
  onError?: () => void;
}

/**
 * ProductImage — accessible, lazy-loaded product image with fallback.
 *
 * - Reserves aspect ratio via CSS to prevent layout shift (CLS).
 * - Shows a branded mushroom placeholder when src is missing or fails.
 * - Decorative placeholder is aria-hidden; real images have meaningful alt.
 * - Lazy loading by default (above-fold detail page uses eager).
 */
export const ProductImage: FC<ProductImageProps> = ({
  src,
  alt,
  loading = 'lazy',
  className = '',
  style = {},
  onError,
}) => {
  const [hasError, setHasError] = useState(false);
  const showFallback = !src || hasError;

  const handleError = () => {
    setHasError(true);
    onError?.();
  };

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
      {!showFallback ? (
        <img
          src={src!}
          alt={alt}
          loading={loading}
          decoding="async"
          onError={handleError}
          style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
        />
      ) : (
        <div
          aria-hidden="true"
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '0.6rem',
            color: 'var(--accent-primary)',
            opacity: 0.7,
            background: 'radial-gradient(circle at 50% 35%, rgba(16, 185, 129, 0.18), transparent 65%)',
            width: '100%',
            height: '100%',
            justifyContent: 'center',
          }}
        >
          <Sprout size={40} strokeWidth={1.5} style={{ filter: 'drop-shadow(0 0 10px rgba(16, 185, 129, 0.35))' }} />
          <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)', fontWeight: 600, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
            Mushroom Spawn
          </span>
        </div>
      )}
    </div>
  );
};
