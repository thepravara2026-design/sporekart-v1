import { FC, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { NavItem } from '../../config/navigation';

interface SidebarProps {
  title?: string;
  items: NavItem[];
  ariaLabel?: string;
}

export const Sidebar: FC<SidebarProps> = ({
  title = 'Navigation',
  items,
  ariaLabel = 'Sidebar navigation',
}) => {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <aside
      aria-label={ariaLabel}
      style={{
        width: collapsed ? '70px' : '260px',
        transition: 'width var(--transition-normal)',
        backgroundColor: 'var(--bg-surface)',
        borderRight: '1px solid var(--border-color)',
        padding: '1.5rem 0.75rem',
        display: 'flex',
        flexDirection: 'column',
        minHeight: 'calc(100vh - 70px)',
      }}
    >
      <div
        style={{
          display: 'flex',
          justifyContent: collapsed ? 'center' : 'space-between',
          alignItems: 'center',
          padding: '0 0.5rem',
          marginBottom: '1.5rem',
        }}
      >
        {!collapsed && (
          <span style={{ fontSize: '0.875rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-secondary)' }}>
            {title}
          </span>
        )}
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => setCollapsed(!collapsed)}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          style={{ padding: '0.35rem' }}
        >
          {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
        </button>
      </div>

      <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
        {items.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            title={collapsed ? item.label : undefined}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              padding: '0.65rem 0.75rem',
              borderRadius: 'var(--radius-md)',
              fontSize: '0.9rem',
              backgroundColor: 'rgba(255,255,255,0.02)',
              textDecoration: 'none',
              overflow: 'hidden',
              whiteSpace: 'nowrap',
            }}
          >
            {!collapsed && <span>{item.label}</span>}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
};
