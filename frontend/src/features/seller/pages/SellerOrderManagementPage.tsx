import React from 'react';
import { SellerLayout } from '../components/SellerLayout';
import { SellerOrderTable } from '../components/SellerOrderTable';
import { useSellerOrders, useTransitionSellerOrder } from '../hooks/useSeller';
import { SellerOrder } from '../types/seller';

export const SellerOrderManagementPage: React.FC = () => {
  const { data: orders, isLoading } = useSellerOrders();
  const transitionMutation = useTransitionSellerOrder();

  const handleTransition = (orderId: string, targetStatus: SellerOrder['status']) => {
    transitionMutation.mutate({ orderId, status: targetStatus });
  };

  return (
    <SellerLayout>
      <div data-testid="seller-order-management-page" className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-100 tracking-tight">Order Fulfillment & Management</h2>
          <p className="text-sm text-slate-400">
            Inspect incoming marketplace orders, update fulfillment state, and trigger shipment dispatches.
          </p>
        </div>

        {/* Orders Table */}
        <SellerOrderTable
          orders={orders || []}
          isLoading={isLoading}
          onTransitionStatus={handleTransition}
        />
      </div>
    </SellerLayout>
  );
};
