import { FC, ReactNode, useState, KeyboardEvent } from 'react';

export interface TabItem {
  id: string;
  label: ReactNode;
  content: ReactNode;
  disabled?: boolean;
}

export interface TabsProps {
  items: TabItem[];
  defaultTabId?: string;
  onChange?: (id: string) => void;
  ariaLabel?: string;
  className?: string;
}

export const Tabs: FC<TabsProps> = ({
  items,
  defaultTabId,
  onChange,
  ariaLabel = 'Navigation tabs',
  className = '',
}) => {
  const [activeTabId, setActiveTabId] = useState<string>(
    defaultTabId || items[0]?.id || ''
  );

  const handleSelectTab = (id: string) => {
    setActiveTabId(id);
    onChange?.(id);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLButtonElement>, index: number) => {
    let nextIndex = index;
    switch (e.key) {
      case 'ArrowRight':
        e.preventDefault();
        nextIndex = (index + 1) % items.length;
        break;
      case 'ArrowLeft':
        e.preventDefault();
        nextIndex = (index - 1 + items.length) % items.length;
        break;
      case 'Home':
        e.preventDefault();
        nextIndex = 0;
        break;
      case 'End':
        e.preventDefault();
        nextIndex = items.length - 1;
        break;
      default:
        return;
    }

    if (items[nextIndex] && !items[nextIndex].disabled) {
      handleSelectTab(items[nextIndex].id);
    }
  };

  return (
    <div className={`tabs-container ${className}`} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div
        role="tablist"
        aria-label={ariaLabel}
        style={{
          display: 'flex',
          gap: '0.5rem',
          borderBottom: '1px solid var(--border-color)',
          paddingBottom: '0.5rem',
          overflowX: 'auto',
        }}
      >
        {items.map((tab, idx) => {
          const isActive = tab.id === activeTabId;
          return (
            <button
              key={tab.id}
              role="tab"
              id={`tab-${tab.id}`}
              aria-selected={isActive}
              aria-controls={`tabpanel-${tab.id}`}
              tabIndex={isActive ? 0 : -1}
              disabled={tab.disabled}
              onClick={() => handleSelectTab(tab.id)}
              onKeyDown={(e) => handleKeyDown(e, idx)}
              className={`tab-button ${isActive ? 'tab-active' : ''}`}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                padding: '0.6rem 1.2rem',
                borderRadius: 'var(--radius-md)',
                fontSize: '0.95rem',
                fontWeight: 600,
                backgroundColor: isActive ? 'var(--accent-primary)' : 'rgba(255, 255, 255, 0.05)',
                color: isActive ? '#ffffff' : 'var(--text-secondary)',
                border: 'none',
                cursor: tab.disabled ? 'not-allowed' : 'pointer',
                opacity: tab.disabled ? 0.5 : 1,
                whiteSpace: 'nowrap',
                transition: 'all var(--transition-fast)',
              }}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      {items.map((tab) => {
        const isActive = tab.id === activeTabId;
        if (!isActive) return null;
        return (
          <div
            key={tab.id}
            role="tabpanel"
            id={`tabpanel-${tab.id}`}
            aria-labelledby={`tab-${tab.id}`}
            tabIndex={0}
            style={{ outline: 'none' }}
          >
            {tab.content}
          </div>
        );
      })}
    </div>
  );
};
