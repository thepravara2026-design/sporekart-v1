import { FC, useState, useRef } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { Menu, ShoppingCart, Sprout } from 'lucide-react';
import { MAIN_NAVIGATION } from '../../config/navigation';
import { MobileNav } from './MobileNav';
import { CartDrawer } from '../../features/cart/components/CartDrawer';
import { useCartCount } from '../../features/cart/hooks/useCartCount';

export const Header: FC = () => {
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const [cartDrawerOpen, setCartDrawerOpen] = useState(false);
  const menuButtonRef = useRef<HTMLButtonElement>(null);
  const cartButtonRef = useRef<HTMLButtonElement>(null);
  const cartCount = useCartCount();

  return (
    <header className="navbar" role="banner">
      <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
        <Link to="/" className="brand" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Sprout style={{ color: 'var(--accent-primary)', width: '28px', height: '28px' }} />
          <span>SPOREKART</span>
        </Link>

        {/* Desktop Primary Navigation */}
        <nav aria-label="Primary navigation" className="nav-links">
          {MAIN_NAVIGATION.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
              end={item.path === '/'}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {/* Cart Quick Trigger — opens the slide-over cart drawer */}
        <button
          ref={cartButtonRef}
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={() => setCartDrawerOpen(true)}
          aria-label={`View Shopping Cart (${cartCount} ${cartCount === 1 ? 'item' : 'items'})`}
          aria-expanded={cartDrawerOpen}
          aria-controls="cart-drawer"
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', position: 'relative' }}
        >
          <ShoppingCart size={18} />
          <span style={{ fontSize: '0.875rem' }}>Cart</span>
          {cartCount > 0 && (
            <span
              data-testid="cart-count-badge"
              aria-hidden="true"
              style={{
                backgroundColor: 'var(--accent-primary)',
                color: '#ffffff',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.7rem',
                fontWeight: 700,
                minWidth: '1.15rem',
                height: '1.15rem',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '0 0.3rem',
                lineHeight: 1,
              }}
            >
              {cartCount}
            </span>
          )}
        </button>

        {/* Mobile Navigation Hamburger Trigger */}
        <button
          ref={menuButtonRef}
          className="mobile-nav-trigger btn btn-secondary btn-sm"
          onClick={() => setMobileNavOpen(true)}
          aria-label="Open mobile navigation menu"
          aria-expanded={mobileNavOpen}
          aria-controls="mobile-nav-drawer"
          style={{ display: 'none' }}
        >
          <Menu size={20} />
        </button>
      </div>

      {/* Mobile Drawer Navigation */}
      <MobileNav
        isOpen={mobileNavOpen}
        onClose={() => setMobileNavOpen(false)}
        items={MAIN_NAVIGATION}
        triggerRef={menuButtonRef}
      />

      {/* Slide-over Cart Drawer */}
      <CartDrawer
        isOpen={cartDrawerOpen}
        onClose={() => setCartDrawerOpen(false)}
        triggerRef={cartButtonRef}
      />
    </header>
  );
};
