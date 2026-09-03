import { FC, useEffect, useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { ShoppingCart } from 'lucide-react';

export interface ProductStickyActionProps {
  productName: string;
  /** Rendered only when the primary purchase panel is scrolled out of view on mobile. */
  visible: boolean;
  isPending: boolean;
  disabled: boolean;
  onAddToCart: () => void;
}

/**
 * ProductStickyAction — mobile-only sticky Add to Cart bar.
 *
 * - Shown only on small viewports (<= 767px) and only while the primary
 *   purchase panel is out of view, so it never duplicates visible controls.
 * - Respects `env(safe-area-inset-bottom)` spacing and carries an accessible
 *   name including the product name.
 * - The trigger re-uses the same cart mutation as the main panel, so duplicate
 *   submissions are prevented while a request is pending.
 */
export const ProductStickyAction: FC<ProductStickyActionProps> = ({
  productName,
  visible,
  isPending,
  disabled,
  onAddToCart,
}) => {
  if (!visible) return null;

  return (
    <div
      className="product-sticky-action"
      style={{
        position: 'fixed',
        left: 0,
        right: 0,
        bottom: 0,
        zIndex: 90,
        padding: '0.75rem 1rem calc(0.75rem + env(safe-area-inset-bottom, 0px))',
        backgroundColor: 'rgba(5, 28, 20, 0.92)',
        backdropFilter: 'blur(12px)',
        borderTop: '1px solid var(--border-color)',
        boxShadow: 'var(--shadow-lg)',
      }}
    >
      <Button
        variant="primary"
        size="lg"
        fullWidth
        disabled={disabled}
        isLoading={isPending}
        leftIcon={!disabled ? <ShoppingCart size={20} /> : undefined}
        onClick={onAddToCart}
        aria-label={`Add ${productName} to cart`}
      >
        {disabled ? 'Unavailable' : 'Add to Cart'}
      </Button>
    </div>
  );
};

/** True when the viewport is a small (mobile) screen. Fails safe to desktop. */
export const useIsMobileViewport = (): boolean => {
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return;
    }
    const mql = window.matchMedia('(max-width: 767px)');
    const update = () => setIsMobile(mql.matches);
    update();
    mql.addEventListener?.('change', update);
    return () => mql.removeEventListener?.('change', update);
  }, []);

  return isMobile;
};

/** True while the referenced element intersects the viewport. Fails safe to "in view". */
export const useElementInViewport = (ref: React.RefObject<HTMLElement | null>): boolean => {
  const [inView, setInView] = useState(true);

  useEffect(() => {
    const element = ref.current;
    if (typeof window === 'undefined' || typeof IntersectionObserver !== 'function' || !element) {
      return;
    }
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => setInView(entry.isIntersecting));
    });
    observer.observe(element);
    return () => observer.disconnect();
  }, [ref]);

  return inView;
};

/**
 * Shared visibility computation for the mobile sticky action.
 * Hidden on desktop, when the purchase panel is on screen, or while an
 * IntersectionObserver/matchMedia capability is unavailable.
 */
export const useStickyActionVisibility = (
  panelRef: React.RefObject<HTMLElement | null>
): boolean => {
  const isMobile = useIsMobileViewport();
  const panelInView = useElementInViewport(panelRef);
  return isMobile && !panelInView;
};