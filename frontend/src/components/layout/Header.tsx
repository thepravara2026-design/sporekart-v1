import { FC, useState, useRef } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { Menu, ShoppingCart, Sprout } from 'lucide-react';
import { MAIN_NAVIGATION } from '../../config/navigation';
import { MobileNav } from './MobileNav';

export const Header: FC = () => {
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const menuButtonRef = useRef<HTMLButtonElement>(null);

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
        {/* Cart Quick Trigger Indicator */}
        <Link
          to="/products"
          className="btn btn-secondary btn-sm"
          aria-label="View Shopping Cart (0 items)"
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
        >
          <ShoppingCart size={18} />
          <span style={{ fontSize: '0.875rem' }}>Cart</span>
        </Link>

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
    </header>
  );
};
