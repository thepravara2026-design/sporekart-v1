import { FC, useState, useRef } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Menu, ShoppingCart, Sprout, LogOut, UserRound } from 'lucide-react';
import { MAIN_NAVIGATION } from '../../config/navigation';
import { MobileNav } from './MobileNav';
import { CartDrawer } from '../../features/cart/components/CartDrawer';
import { useCartCount } from '../../features/cart/hooks/useCartCount';
import { useAuth } from '../../context/AuthContext';

export const Header: FC = () => {
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const [cartDrawerOpen, setCartDrawerOpen] = useState(false);
  const menuButtonRef = useRef<HTMLButtonElement>(null);
  const cartButtonRef = useRef<HTMLButtonElement>(null);
  const cartCount = useCartCount();
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="navbar" role="banner">
      <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
        <Link to="/" className="brand" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '34px', height: '34px', borderRadius: 'var(--radius-md)', background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(16, 185, 129, 0.05))', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
            <Sprout style={{ color: 'var(--accent-primary)', width: '22px', height: '22px' }} />
          </span>
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
                minWidth: '1.25rem',
                height: '1.25rem',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '0 0.3rem',
                lineHeight: 1,
                border: '2px solid rgba(5, 28, 20, 0.9)',
                boxShadow: '0 0 0 1px rgba(16, 185, 129, 0.5), 0 2px 6px rgba(0, 0, 0, 0.35)',
                position: 'absolute',
                top: '-0.45rem',
                right: '-0.45rem',
              }}
            >
              {cartCount}
            </span>
          )}
        </button>

        {/* Auth Controls — sign in / register when logged out, user + logout when logged in */}
        {isAuthenticated ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span
              data-testid="header-user-name"
              title={user?.email}
              style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-primary)' }}
            >
              <UserRound size={16} style={{ color: 'var(--accent-primary)' }} />
              {user?.name}
            </span>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={handleLogout}
              aria-label="Sign out"
              style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
            >
              <LogOut size={15} />
              <span>Sign Out</span>
            </button>
          </div>
        ) : (
          <Link to="/login" className="btn btn-outline btn-sm nav-signin" aria-label="Sign in">
            <UserRound size={15} />
            <span>Sign In</span>
          </Link>
        )}

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
