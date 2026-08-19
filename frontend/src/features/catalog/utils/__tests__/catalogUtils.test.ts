import { describe, it, expect } from 'vitest';
import { getStockAvailabilityInfo } from '../catalogUtils';

describe('getStockAvailabilityInfo', () => {
  it('returns "In Stock" (NORMAL) when availableStock >= 10', () => {
    expect(getStockAvailabilityInfo('ACTIVE', 100)).toEqual({
      state: 'NORMAL',
      label: 'In Stock',
      badgeVariant: 'success',
      isPurchasable: true,
    });

    expect(getStockAvailabilityInfo('ACTIVE', 10)).toEqual({
      state: 'NORMAL',
      label: 'In Stock',
      badgeVariant: 'success',
      isPurchasable: true,
    });
  });

  it('returns "Limited Stock" (LIMITED) when 5 <= availableStock < 10', () => {
    expect(getStockAvailabilityInfo('ACTIVE', 9)).toEqual({
      state: 'LIMITED',
      label: 'Limited Stock',
      badgeVariant: 'warning',
      isPurchasable: true,
    });

    expect(getStockAvailabilityInfo('ACTIVE', 5)).toEqual({
      state: 'LIMITED',
      label: 'Limited Stock',
      badgeVariant: 'warning',
      isPurchasable: true,
    });
  });

  it('returns "Order Now" (ORDER_NOW) when 0 < availableStock < 5', () => {
    expect(getStockAvailabilityInfo('ACTIVE', 4)).toEqual({
      state: 'ORDER_NOW',
      label: 'Order Now',
      badgeVariant: 'warning',
      isPurchasable: true,
    });

    expect(getStockAvailabilityInfo('ACTIVE', 1)).toEqual({
      state: 'ORDER_NOW',
      label: 'Order Now',
      badgeVariant: 'warning',
      isPurchasable: true,
    });
  });

  it('returns "Out of Stock" (OUT_OF_STOCK) when availableStock <= 0', () => {
    expect(getStockAvailabilityInfo('ACTIVE', 0)).toEqual({
      state: 'OUT_OF_STOCK',
      label: 'Out of Stock',
      badgeVariant: 'danger',
      isPurchasable: false,
    });

    expect(getStockAvailabilityInfo('ACTIVE', -5)).toEqual({
      state: 'OUT_OF_STOCK',
      label: 'Out of Stock',
      badgeVariant: 'danger',
      isPurchasable: false,
    });
  });

  it('returns "Out of Stock" when product or variant status is OUT_OF_STOCK, DISCONTINUED, or ARCHIVED', () => {
    expect(getStockAvailabilityInfo('OUT_OF_STOCK', 50)).toEqual({
      state: 'OUT_OF_STOCK',
      label: 'Out of Stock',
      badgeVariant: 'danger',
      isPurchasable: false,
    });

    expect(getStockAvailabilityInfo('DISCONTINUED', 50)).toEqual({
      state: 'OUT_OF_STOCK',
      label: 'Discontinued',
      badgeVariant: 'danger',
      isPurchasable: false,
    });
  });
});
