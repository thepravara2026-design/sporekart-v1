import { describe, it, expect } from 'vitest';
import { ApiError } from '../../../../services/apiError';
import {
  getOrderErrorMessage,
  getShipmentErrorMessage,
  formatOrderDate,
  getShipmentStatusLabel,
  getShipmentStatusVariant,
} from '../orderUtils';
import { getOrderStatusLabel, getOrderStatusVariant } from '../orderStatus';

describe('orderUtils (FD-13)', () => {
  describe('getOrderErrorMessage', () => {
    it('maps ORDER_NOT_FOUND to a friendly message', () => {
      expect(getOrderErrorMessage(new ApiError('nope', 'ORDER_NOT_FOUND', 404))).toContain(
        'Order not found'
      );
    });

    it('maps ORDER_ACCESS_DENIED and ORDER_NOT_CANCELLABLE', () => {
      expect(getOrderErrorMessage(new ApiError('nope', 'ORDER_ACCESS_DENIED', 403))).toContain(
        'do not have access'
      );
      expect(getOrderErrorMessage(new ApiError('nope', 'ORDER_NOT_CANCELLABLE', 400))).toContain(
        'can no longer be cancelled'
      );
    });

    it('maps 401 to an authentication message', () => {
      expect(getOrderErrorMessage(new ApiError('nope', 'UNAUTHORIZED', 401))).toContain(
        'sign in'
      );
    });

    it('falls back gracefully for non-API errors', () => {
      expect(getOrderErrorMessage(new Error('boom'))).toContain('try again');
      expect(getOrderErrorMessage('nonsense')).toContain('try again');
    });
  });

  describe('getShipmentErrorMessage', () => {
    it('explains a missing shipment for 404', () => {
      expect(getShipmentErrorMessage(new ApiError('missing', 'SHIPMENT_NOT_FOUND', 404))).toContain(
        'No shipment is available'
      );
    });

    it('falls back for other failures', () => {
      expect(getShipmentErrorMessage(new Error('boom'))).toContain('try again');
    });
  });

  describe('formatOrderDate', () => {
    it('formats an ISO timestamp', () => {
      expect(formatOrderDate('2026-08-18T11:00:00Z')).toMatch(/18 Aug 2026/);
    });

    it('returns an empty string for falsy input', () => {
      expect(formatOrderDate('')).toBe('');
      expect(formatOrderDate(null)).toBe('');
    });

    it('returns the raw value when the date is invalid', () => {
      expect(formatOrderDate('not-a-date')).toBe('not-a-date');
    });
  });

  describe('shipment status labels & variants', () => {
    it('labels delivery states', () => {
      expect(getShipmentStatusLabel('IN_TRANSIT')).toBe('In Transit');
      expect(getShipmentStatusLabel('DELIVERED')).toBe('Delivered');
      expect(getShipmentStatusLabel('CANCELLED')).toBe('Shipment Cancelled');
      expect(getShipmentStatusLabel('UNKNOWN')).toBe('UNKNOWN');
    });

    it('maps delivery states to badge variants', () => {
      expect(getShipmentStatusVariant('DELIVERED')).toBe('success');
      expect(getShipmentStatusVariant('CANCELLED')).toBe('danger');
      expect(getShipmentStatusVariant('DELIVERY_FAILED')).toBe('warning');
      expect(getShipmentStatusVariant('IN_TRANSIT')).toBe('info');
      expect(getShipmentStatusVariant('CREATED')).toBe('neutral');
    });
  });

  describe('order status utils (moved into orders feature)', () => {
    it('labels all backend statuses', () => {
      expect(getOrderStatusLabel('PAYMENT_PENDING')).toBe('Payment Pending');
      expect(getOrderStatusLabel('DELIVERED')).toBe('Delivered');
      expect(getOrderStatusLabel('CANCELLED')).toBe('Cancelled');
    });

    it('maps statuses to badge variants', () => {
      expect(getOrderStatusVariant('CONFIRMED')).toBe('success');
      expect(getOrderStatusVariant('PROCESSING')).toBe('info');
      expect(getOrderStatusVariant('EXPIRED')).toBe('warning');
      expect(getOrderStatusVariant('CANCELLED')).toBe('danger');
    });
  });
});
