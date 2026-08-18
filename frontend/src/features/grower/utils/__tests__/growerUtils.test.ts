import { describe, it, expect } from 'vitest';
import { formatCurrency, getInventoryStatusMeta, getOrderStatusMeta, getProductStatusMeta } from '../growerUtils';

describe('growerUtils', () => {
  it('formats currency correctly', () => {
    expect(formatCurrency(120)).toBe('$120.00');
    expect(formatCurrency(45.5, 'USD')).toBe('$45.50');
  });

  it('resolves product status metadata', () => {
    expect(getProductStatusMeta('ACTIVE').label).toBe('Active');
    expect(getProductStatusMeta('ACTIVE').variant).toBe('success');
    expect(getProductStatusMeta('DRAFT').variant).toBe('warning');
  });

  it('calculates inventory status metadata', () => {
    expect(getInventoryStatusMeta(0, 0).label).toBe('Out of Stock');
    expect(getInventoryStatusMeta(15, 10, 10).label).toBe('Low Stock');
    expect(getInventoryStatusMeta(50, 5, 10).label).toBe('Healthy');
  });

  it('resolves order status metadata', () => {
    expect(getOrderStatusMeta('PROCESSING').label).toBe('Processing');
    expect(getOrderStatusMeta('COMPLETED').variant).toBe('success');
  });
});
