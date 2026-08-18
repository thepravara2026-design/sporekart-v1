import { FC, useState, KeyboardEvent } from 'react';
import { Dialog } from '../../../components/ui/Dialog';
import { Sprout, Maximize2 } from 'lucide-react';

export interface ProductGalleryProps {
  images?: string[];
  productName: string;
  className?: string;
}

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

  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>, index: number) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      setSelectedIndex(index);
    } else if (e.key === 'ArrowRight') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev + 1) % displayImages.length);
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev - 1 + displayImages.length) % displayImages.length);
    }
  };

  return (
    <div className={`product-gallery ${className}`} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', width: '100%' }}>
      {/* Main Image */}
      <div
        className="product-gallery-main"
        onClick={() => currentImage && !hasError && setIsLightboxOpen(true)}
        style={{
          width: '100%',
          aspectRatio: '4 / 3',
          backgroundColor: 'rgba(5, 28, 20, 0.6)',
          borderRadius: 'var(--radius-lg)',
          overflow: 'hidden',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          position: 'relative',
          cursor: currentImage && !hasError ? 'zoom-in' : 'default',
          border: '1px solid var(--border-color)',
        }}
      >
        {currentImage && !hasError ? (
          <>
            <img
              src={currentImage}
              alt={`${productName} view ${selectedIndex + 1}`}
              onError={() => setHasError(true)}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
            <div
              style={{
                position: 'absolute',
                right: '0.75rem',
                bottom: '0.75rem',
                background: 'rgba(0, 0, 0, 0.6)',
                padding: '0.35rem 0.6rem',
                borderRadius: 'var(--radius-md)',
                color: 'var(--text-primary)',
                display: 'flex',
                alignItems: 'center',
                gap: '0.25rem',
                fontSize: '0.75rem',
              }}
            >
              <Maximize2 size={14} /> Zoom
            </div>
          </>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem', color: 'var(--accent-primary)' }}>
            <Sprout size={48} />
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              {productName} Spawn Specimen
            </span>
          </div>
        )}
      </div>

      {/* Thumbnails */}
      {displayImages.length > 1 && (
        <div
          className="product-gallery-thumbnails"
          role="region"
          aria-label="Product images gallery"
          style={{ display: 'flex', gap: '0.75rem', overflowX: 'auto', paddingBottom: '0.25rem' }}
        >
          {displayImages.map((img, idx) => (
            <div
              key={idx}
              role="button"
              tabIndex={0}
              aria-label={`View ${productName} image ${idx + 1}`}
              aria-selected={idx === selectedIndex}
              onClick={() => setSelectedIndex(idx)}
              onKeyDown={(e) => handleKeyDown(e, idx)}
              style={{
                width: '72px',
                height: '72px',
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
                cursor: 'pointer',
                border: idx === selectedIndex ? '2px solid var(--accent-primary)' : '1px solid var(--border-color)',
                opacity: idx === selectedIndex ? 1 : 0.6,
                transition: 'all var(--transition-fast)',
                flexShrink: 0,
              }}
            >
              <img
                src={img}
                alt={`${productName} thumbnail ${idx + 1}`}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>
          ))}
        </div>
      )}

      {/* Lightbox Dialog */}
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
