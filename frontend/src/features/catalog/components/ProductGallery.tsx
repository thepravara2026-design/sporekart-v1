import { FC, useState, useCallback, KeyboardEvent } from 'react';
import { Dialog } from '../../../components/ui/Dialog';
import { ProductImage } from './ProductImage';
import { ChevronLeft, ChevronRight, Maximize2 } from 'lucide-react';

export interface ProductGalleryProps {
  /** Backend-provided product image URLs. When empty a branded placeholder is shown. */
  images?: string[];
  productName: string;
  className?: string;
}

const IMG_BUTTON_STYLE: React.CSSProperties = {
  width: '40px',
  height: '40px',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  borderRadius: 'var(--radius-md)',
  backgroundColor: 'rgba(0, 0, 0, 0.55)',
  border: '1px solid var(--border-color)',
  color: 'var(--text-primary)',
  cursor: 'pointer',
  transition: 'background-color var(--transition-fast)',
};

/**
 * ProductGallery — accessible product image gallery.
 *
 * - Single-image products render a plain primary image with no gallery controls.
 * - Multi-image products render previous/next controls and selectable thumbnails.
 * - Every control is a real <button> with an accessible name.
 * - Thumbnails and non-primary images are lazy-loaded; the primary image is eager.
 * - A reserved aspect ratio prevents layout shift (CLS).
 * - Falls back to a branded placeholder when an image is missing or fails to load.
 */
export const ProductGallery: FC<ProductGalleryProps> = ({
  images = [],
  productName,
  className = '',
}) => {
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [isLightboxOpen, setIsLightboxOpen] = useState(false);
  const [hasError, setHasError] = useState(false);

  const displayImages = images.length > 0 ? images : [];
  const currentImage = displayImages[selectedIndex];
  const hasMultiple = displayImages.length > 1;

  const selectImage = useCallback((index: number) => {
    const safeIndex = ((index % displayImages.length) + displayImages.length) % displayImages.length;
    setSelectedIndex(safeIndex);
    setHasError(false);
  }, [displayImages.length]);

  const showPrevious = useCallback(() => {
    selectImage(selectedIndex - 1);
  }, [selectImage, selectedIndex]);

  const showNext = useCallback(() => {
    selectImage(selectedIndex + 1);
  }, [selectImage, selectedIndex]);

  const handleThumbnailsKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (!hasMultiple) return;
    if (e.key === 'ArrowLeft') {
      e.preventDefault();
      selectImage(selectedIndex - 1);
    } else if (e.key === 'ArrowRight') {
      e.preventDefault();
      selectImage(selectedIndex + 1);
    } else if (e.key === 'Home') {
      e.preventDefault();
      selectImage(0);
    } else if (e.key === 'End') {
      e.preventDefault();
      selectImage(displayImages.length - 1);
    }
  };

  const mainAlt = currentImage ? `${productName} image ${selectedIndex + 1}` : productName;

  return (
    <div className={`product-gallery ${className}`} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', width: '100%' }}>
      <div style={{ position: 'relative' }}>
        <ProductImage
          src={currentImage}
          alt={mainAlt}
          loading={currentImage && selectedIndex === 0 ? 'eager' : 'lazy'}
          onError={() => setHasError(true)}
          style={{ borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)' }}
        />

        {currentImage && !hasError && (
          <button
            type="button"
            aria-label={`Zoom in on ${mainAlt}`}
            onClick={() => setIsLightboxOpen(true)}
            style={{
              position: 'absolute',
              right: '0.75rem',
              bottom: '0.75rem',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem',
              padding: '0.5rem 0.75rem',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'rgba(0, 0, 0, 0.6)',
              border: '1px solid var(--border-color)',
              color: 'var(--text-primary)',
              fontSize: '0.75rem',
              fontWeight: 600,
              cursor: 'pointer',
              transition: 'background-color var(--transition-fast)',
            }}
          >
            <Maximize2 size={14} /> Zoom
          </button>
        )}

        {hasMultiple && currentImage && (
          <>
            <button
              type="button"
              aria-label="Previous product image"
              onClick={showPrevious}
              style={{ ...IMG_BUTTON_STYLE, position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }}
            >
              <ChevronLeft size={20} />
            </button>
            <button
              type="button"
              aria-label="Next product image"
              onClick={showNext}
              style={{ ...IMG_BUTTON_STYLE, position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)' }}
            >
              <ChevronRight size={20} />
            </button>
          </>
        )}
      </div>

      {hasMultiple && (
        <div
          role="group"
          aria-label="Product images"
          onKeyDown={handleThumbnailsKeyDown}
          style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}
        >
          {displayImages.map((img, idx) => (
            <button
              key={idx}
              type="button"
              aria-label={`View ${productName} image ${idx + 1}`}
              aria-pressed={idx === selectedIndex}
              onClick={() => selectImage(idx)}
              style={{
                width: '72px',
                height: '72px',
                padding: 0,
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
                cursor: 'pointer',
                border: idx === selectedIndex ? '2px solid var(--accent-primary)' : '1px solid var(--border-color)',
                opacity: idx === selectedIndex ? 1 : 0.6,
                transition: 'all var(--transition-fast)',
                flexShrink: 0,
                backgroundColor: 'transparent',
              }}
            >
              <ProductImage
                src={img}
                alt={`${productName} thumbnail ${idx + 1}`}
                loading="lazy"
                style={{ width: '100%', height: '100%', aspectRatio: '1 / 1', borderRadius: 0, border: 'none' }}
              />
            </button>
          ))}
        </div>
      )}

      {currentImage && (
        <Dialog
          isOpen={isLightboxOpen}
          onClose={() => setIsLightboxOpen(false)}
          title={`${productName} - Image View`}
        >
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '1rem 0' }}>
            <img
              src={currentImage}
              alt={`${productName} enlarged view`}
              style={{ maxWidth: '100%', maxHeight: '70vh', objectFit: 'contain', borderRadius: 'var(--radius-md)' }}
            />
          </div>
        </Dialog>
      )}
    </div>
  );
};
