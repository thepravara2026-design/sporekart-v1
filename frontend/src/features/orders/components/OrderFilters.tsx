import { FC } from 'react';
import { Select } from '../../../components/ui/Select';
import { ORDER_STATUS_FILTERS, OrderStatusFilter } from '../constants/orderConstants';
import { getOrderStatusLabel } from '../utils/orderStatus';

export interface OrderFiltersProps {
  value: OrderStatusFilter;
  onChange: (value: OrderStatusFilter) => void;
}

/** Status filter for the order history list. */
export const OrderFilters: FC<OrderFiltersProps> = ({ value, onChange }) => {
  const options = ORDER_STATUS_FILTERS.map((filter) => ({
    value: filter,
    label: filter === 'ALL' ? 'All Orders' : getOrderStatusLabel(filter),
  }));

  return (
    <div data-testid="order-filters" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
      <label htmlFor="order-status-filter" style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-secondary)' }}>
        Filter by status
      </label>
      <Select
        id="order-status-filter"
        aria-label="Filter orders by status"
        value={value}
        options={options}
        onChange={(e) => onChange(e.target.value as OrderStatusFilter)}
        style={{ width: '220px' }}
      />
    </div>
  );
};