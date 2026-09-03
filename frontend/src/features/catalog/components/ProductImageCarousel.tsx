import { FC, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { ProductImage } from './ProductImage';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import {
  PRODUCT_IMAGE_CAROUSEL_INTERVAL,
  PRODUCT_IMAGE_CAROUSEL_SWIPE_THRESHOLD,
} from '../constants/catalogConstants';

export interface ProductImageCarouselProps {
  /** Backend-provided product image URLs. Empty or single-image inputs render statically. */
  images?: string[];
  alt: string;
  /** Autoplay on/off. Disabled automatically for reduced-motion users. Default true. */
  autoPlay?: boolean;
  /** Autoplay interval in ms. Defaults to PRODUCT_IMAGE_CAROUSEL_INTERVAL. */
  interval?: number;
  /** Lazy loading for off-screen product cards. Default 'lazy'. */
  loading?: 'eager' | 'lazy';
  className?: string;
  style?: React.CSSProperties;
}

const CONTROL_BUTTON_STYLE: React.CSSProperties = {
  position: 'absolute',
  top: '50%',
  transform: 'translateY(-50%)',
  zIndex: 3,
  width: '30px',
  height: '30px',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  borderRadius: 'var(--radius-full)',
  backgroundColor: 'rgba(0, 0, 0, 0.55)',
  border: '1px solid rgba(255, 255, 255, 0.25)',
  color: 'var(--text-primary)',
  cursor: 'pointer',
  opacity: 0,
  transition: 'opacity var(--transition-fast), background-color var(--transition-fast)',
};

/**
 * ProductImageCarousel — auto-sliding, accessible product image carousel.
 *
 * - Local state only: `activeImageIndex`, `isPaused`, `isHovered`.
 * - Autoplay runs on a stable interval (default 3500ms) and is cleaned up on
 *   unmount; it pauses on hover/focus/touch and for reduced-motion users.
 * - Manual selection (arrows/indicators/swipe) continues from the chosen slide.
 * - Controls stopPropagation so the surrounding product-card Link never navigates.
 * - Swipe gestures are tracked and suppressed so a swipe does not trigger a tap-through.
 * - Empty or single-image inputs render a static image (no controls, no timers).
 * - Broken images fall back to the branded placeholder.
 */
export const ProductImageCarousel: FC<ProductImageCarouselProps> = ({
  images = [],
  alt,
  autoPlay = true,
  interval = PRODUCT_IMAGE_CAROUSEL_INTERVAL,
  loading = 'lazy',
  className = '',
  style = {},
}) => {
  const displayImages = useMemo(
    () => images.filter((src): src is string => Boolean(src && src.trim())),
    [images]
  );
  const count = displayImages.length;

  const [activeIndex, setActiveIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const [reducedMotion, setReducedMotion] = useState(false);
  const [failedIndexes, setFailedIndexes] = useState<Set<number>>(() => new Set());

  const touchStartX = useRef<number | null>(null);
  const touchStartY = useRef<number | null>(null);
  const didSwipe = useRef(false);

  useEffect(() => {
    setActiveIndex(0);
    setFailedIndexes(new Set());
  }, [displayImages]);

  useEffect(() => {
    const mq = typeof window !== 'undefined' ? window.matchMedia?.('(prefers-reduced-motion: reduce)') : undefined;
    if (!mq) return;
    const update = () => setReducedMotion(mq.matches);
    update();
    mq.addEventListener?.('change', update);
    return () => mq.removeEventListener?.('change', update);
  }, []);

  const goTo = useCallback(
    (index: number) => {
      if (count === 0) return;
      const safe = ((index % count) + count) % count;
      setActiveIndex(safe);
    },
    [count]
  );

  const nextActive = useCallback(() => {
    setActiveIndex((prev) => {
      let candidate = (prev + 1) % count;
      // Skip slides whose image failed to load.
      let guard = 0;
      while (failedIndexes.has(candidate) && guard < count) {
        candidate = (candidate + 1) % count;
        guard++;
      }
      return candidate;
    });
  }, [count, failedIndexes]);

  const prevActive = useCallback(() => {
    setActiveIndex((prev) => {
      let candidate = ((prev - 1) % count + count) % count;
      let guard = 0;
      while (failedIndexes.has(candidate) && guard < count) {
        candidate = ((candidate - 1) % count + count) % count;
        guard++;
      }
      return candidate;
    });
  }, [count, failedIndexes]);

  // Stable autoplay lifecycle: only one interval per mounted carousel, cleaned
  // up on unmount and disabled when paused/hovered/reduced-motion/single-image.
  useEffect(() => {
    if (count <= 1 || !autoPlay || reducedMotion || isPaused || isHovered) return;
    const id = window.setInterval(nextActive, interval);
    return () => window.clearInterval(id);
  }, [count, autoPlay, reducedMotion, isPaused, isHovered, interval, nextActive]);

  const handleImageError = useCallback(
    (index: number) => {
      setFailedIndexes((prev) => {
        if (prev.has(index)) return prev;
        const next = new Set(prev);
        next.add(index);
        return next;
      });
    },
    []
  );

  const handleControlPress = useCallback(
    (e: React.MouseEvent | React.KeyboardEvent, action: () => void) => {
      e.preventDefault();
      e.stopPropagation();
      action();
    },
    []
  );

  const handleTouchStart = (e: React.TouchEvent) => {
    touchStartX.current = e.touches[0].clientX;
    touchStartY.current = e.touches[0].clientY;
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    if (touchStartX.current === null) return;
    const dx = e.changedTouches[0].clientX - touchStartX.current;
    const dy = (touchStartY.current ?? 0) - e.changedTouches[0].clientY;
    touchStartX.current = null;
    touchStartY.current = null;

    // Only treat clearly-horizontal swipes as navigation (no vertical scroll intent).
    if (Math.abs(dx) < PRODUCT_IMAGE_CAROUSEL_SWIPE_THRESHOLD || Math.abs(dx) <= Math.abs(dy)) {
      return;
    }
    didSwipe.current = true;
    if (dx < 0) {
      nextActive();
    } else {
      prevActive();
    }
  };

  // Suppress the click that follows a swipe so the card Link never navigates.
  const handleClick = (e: React.MouseEvent) => {
    if (didSwipe.current) {
      didSwipe.current = false;
      e.preventDefault();
      e.stopPropagation();
    }
  };

  if (count === 0) {
    return <ProductImage src={undefined} alt={alt} loading={loading} className={className} style={style} />;
  }

  if (count === 1) {
    return (
      <ProductImage
        src={displayImages[0]}
        alt={alt}
        loading={loading}
        className={className}
        style={style}
      />
    );
  }

  const slides = displayImages.map((src, index) => {
    const isActive = index === activeIndex;
    return (
      <div
        key={index}
        className={`product-carousel-slide${isActive ? ' is-active' : ''}`}
        aria-hidden={!isActive}
        style={{
          position: 'absolute',
          inset: 0,
          opacity: isActive ? 1 : 0,
          transition: 'opacity 0.45s ease-in-out',
          pointerEvents: isActive ? 'auto' : 'none',
          zIndex: isActive ? 2 : 1,
        }}
      >
        <ProductImage
          src={src}
          alt={isActive ? alt : `${alt} (view ${index + 1} of ${count})`}
          loading={loading}
          onError={() => handleImageError(index)}
          style={{ width: '100%', height: '100%', aspectRatio: 'auto', borderRadius: 0 }}
        />
      </div>
    );
  });

  return (
    <div
      className={`product-image-carousel ${className}`}
      style={{
        position: 'relative',
        aspectRatio: '4 / 3',
        overflow: 'hidden',
        borderRadius: 'var(--radius-md)',
        backgroundColor: 'rgba(5, 28, 20, 0.6)',
        touchAction: 'pan-y',
        ...style,
      }}
      role="region"
      aria-roledescription="carousel"
      aria-label={`${alt} images`}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onFocus={() => setIsPaused(true)}
      onBlur={() => setIsPaused(false)}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      onClick={handleClick}
    >
      {slides}

      <button
        type="button"
        aria-label="Previous product image"
        onClick={(e) => handleControlPress(e, prevActive)}
        style={{ ...CONTROL_BUTTON_STYLE, left: '0.5rem' }}
        className="product-carousel-arrow"
      >
        <ChevronLeft size={18} />
      </button>
      <button
        type="button"
        aria-label="Next product image"
        onClick={(e) => handleControlPress(e, nextActive)}
        style={{ ...CONTROL_BUTTON_STYLE, right: '0.5rem' }}
        className="product-carousel-arrow"
      >
        <ChevronRight size={18} />
      </button>

      <div
        className="product-carousel-indicators"
        role="group"
        aria-label={`${alt} image selection`}
        style={{
          position: 'absolute',
          bottom: '0.5rem',
          left: '50%',
          transform: 'translateX(-50%)',
          zIndex: 3,
          display: 'flex',
          gap: '0.4rem',
          padding: '0.2rem 0.5rem',
          borderRadius: 'var(--radius-full)',
          backgroundColor: 'rgba(0, 0, 0, 0.45)',
        }}
      >
        {displayImages.map((_, index) => {
          const isActive = index === activeIndex;
          return (
            <button
              key={index}
              type="button"
              aria-label={`View image ${index + 1} of ${count}`}
              aria-current={isActive ? 'true' : undefined}
              onClick={(e) => handleControlPress(e, () => goTo(index))}
              style={{
                width: isActive ? '16px' : '7px',
                height: '7px',
                padding: 0,
                borderRadius: 'var(--radius-full)',
                border: 'none',
                backgroundColor: isActive ? 'var(--accent-primary, #10b981)' : 'rgba(255, 255, 255, 0.55)',
                cursor: 'pointer',
                transition: 'all var(--transition-fast)',
              }}
            />
          );
        })}
      </div>

      {/* Active slide is referenced for screen readers; hidden slides are aria-hidden. */}
      <span
        aria-live="polite"
        style={{ position: 'absolute', width: '1px', height: '1px', margin: '-1px', padding: 0, overflow: 'hidden', clip: 'rect(0 0 0 0)', clipPath: 'inset(50%)', whiteSpace: 'nowrap', border: 0 }}
      >
        {`Viewing image ${activeIndex + 1} of ${count}`}
      </span>
    </div>
  );
};