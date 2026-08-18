import React from 'react';
import { Badge } from '../../../components/ui/Badge';
import { Button } from '../../../components/ui/Button';
import { SellerOrder } from '../types/seller';

export interface SellerOrderTableProps {
  orders: SellerOrder[];
  isLoading?: boolean;
  onTransitionStatus?: (orderId: string, targetStatus: SellerOrder['status']) => void;
  testId?: string;
}

export const SellerOrderTable: React.FC<SellerOrderTableProps> = ({
  orders,
  isLoading = false,
  onTransitionStatus,
  testId = 'seller-order-table',
}) => {
  if (isLoading) {
    return (
      <div data-testid={`${testId}-loading`} className="p-8 text-center text-slate-400">
        Loading seller orders...
      </div>
    );
  }

  if (!orders || orders.length === 0) {
    return (
      <div data-testid={`${testId}-empty`} className="p-8 text-center text-slate-400">
        No customer orders assigned to seller yet.
      </div>
    );
  }

  const getBadgeVariant = (status: SellerOrder['status']) => {
    switch (status) {
      case 'DELIVERED':
        return 'success';
      case 'SHIPPED':
        return 'info';
      case 'PROCESSING':
        return 'warning';
      case 'CANCELLED':
        return 'danger';
      default:
        return 'neutral';
    }
  };

  return (
    <div data-testid={testId} className="overflow-x-auto rounded-lg border border-slate-800 bg-slate-900/40">
      <table className="w-full text-left text-sm text-slate-200">
        <thead className="bg-slate-800/80 text-xs uppercase tracking-wider text-slate-400 border-b border-slate-800">
          <tr>
            <th className="px-4 py-3 font-semibold">Order #</th>
            <th className="px-4 py-3 font-semibold">Customer</th>
            <th className="px-4 py-3 font-semibold">Items</th>
            <th className="px-4 py-3 font-semibold">Total Amount</th>
            <th className="px-4 py-3 font-semibold">Status</th>
            <th className="px-4 py-3 font-semibold">Order Date</th>
            <th className="px-4 py-3 font-semibold">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800/60">
          {orders.map((order) => (
            <tr key={order.id} className="hover:bg-slate-800/30 transition-colors">
              <td className="px-4 py-3 font-mono font-medium text-forest-400">{order.orderNumber}</td>
              <td className="px-4 py-3 text-slate-100">
                <div>{order.customerName}</div>
                <div className="text-xs text-slate-400">{order.customerEmail}</div>
              </td>
              <td className="px-4 py-3 text-slate-300">{order.itemsCount} items</td>
              <td className="px-4 py-3 font-semibold text-slate-100">
                ${order.totalAmount.toFixed(2)}
              </td>
              <td className="px-4 py-3">
                <Badge variant={getBadgeVariant(order.status)}>{order.status}</Badge>
              </td>
              <td className="px-4 py-3 text-xs text-slate-400">
                {new Date(order.orderDate).toLocaleDateString()}
              </td>
              <td className="px-4 py-3">
                {onTransitionStatus && order.status === 'PROCESSING' && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => onTransitionStatus(order.id, 'SHIPPED')}
                    className="text-xs border-forest-600 text-forest-300 hover:bg-forest-950/40"
                  >
                    Mark Shipped
                  </Button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
