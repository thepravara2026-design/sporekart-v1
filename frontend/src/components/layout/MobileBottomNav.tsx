import { FC } from 'react';
import { NavLink } from 'react-router-dom';
import { Home, Store, ShoppingCart, User, Sprout } from 'lucide-react';
import { useCart } from '../../features/cart/hooks/useCart';

export const MobileBottomNav: FC = () => {
  const { data: cartResponse } = useCart();
  const itemCount = cartResponse?.data?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;

  const navItems = [
    { to: '/', label: 'Home', icon: Home, end: true },
    { to: '/products', label: 'Catalog', icon: Store },
    { to: '/cart', label: 'Cart', icon: ShoppingCart, badge: itemCount },
    { to: '/grower', label: 'Grower', icon: Sprout },
    { to: '/login', label: 'Account', icon: User },
  ];

  return (
    <nav
      aria-label="Mobile bottom navigation"
      className="mobile-bottom-nav"
      style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        height: '60px',
        background: '#ffffff',
        borderTop: '1px solid #e2e8f0',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-around',
        zIndex: 50,
        boxShadow: '0 -2px 10px rgba(0,0,0,0.05)',
      }}
    >
      {navItems.map(item => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            style={({ isActive }) => ({
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '2px',
              textDecoration: 'none',
              fontSize: '0.6875rem',
              fontWeight: 500,
              color: isActive ? '#059669' : '#64748b',
              position: 'relative',
              padding: '6px 12px',
            })}
          >
            <div style={{ position: 'relative' }}>
              <Icon size={20} />
              {!!item.badge && item.badge > 0 && (
                <span
                  style={{
                    position: 'absolute',
                    top: '-4px',
                    right: '-8px',
                    background: '#dc2626',
                    color: '#ffffff',
                    fontSize: '0.625rem',
                    fontWeight: 700,
                    borderRadius: '10px',
                    padding: '2px 5px',
                    minWidth: '14px',
                    textAlign: 'center',
                    lineHeight: 1,
                  }}
                >
                  {item.badge}
                </span>
              )}
            </div>
            <span>{item.label}</span>
          </NavLink>
        );
      })}
    </nav>
  );
};
