import React from 'react';
import { Badge } from '../../../components/ui/Badge';
import { InventoryStatusBadge } from './InventoryStatusBadge';
import { SellerProductItem } from '../types/seller';

export interface SellerProductTableProps {
  products: SellerProductItem[];
  isLoading?: boolean;
  testId?: string;
}

export const SellerProductTable: React.FC<SellerProductTableProps> = ({
  products,
  isLoading = false,
  testId = 'seller-product-table',
}) => {
  if (isLoading) {
    return (
      <div data-testid={`${testId}-loading`} className="p-8 text-center text-slate-400">
        Loading product listings...
      </div>
    );
  }

  if (!products || products.length === 0) {
    return (
      <div data-testid={`${testId}-empty`} className="p-8 text-center text-slate-400">
        No products listed yet. Create a new listing to get started.
      </div>
    );
  }

  return (
    <div data-testid={testId} className="overflow-x-auto rounded-lg border border-slate-800 bg-slate-900/40">
      <table className="w-full text-left text-sm text-slate-200">
        <thead className="bg-slate-800/80 text-xs uppercase tracking-wider text-slate-400 border-b border-slate-800">
          <tr>
            <th className="px-4 py-3 font-semibold">Product Name</th>
            <th className="px-4 py-3 font-semibold">SKU</th>
            <th className="px-4 py-3 font-semibold">Category</th>
            <th className="px-4 py-3 font-semibold">Price</th>
            <th className="px-4 py-3 font-semibold">On-Hand</th>
            <th className="px-4 py-3 font-semibold">Sync Status</th>
            <th className="px-4 py-3 font-semibold">Status</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800/60">
          {products.map((product) => (
            <tr key={product.id} className="hover:bg-slate-800/30 transition-colors">
              <td className="px-4 py-3 font-medium text-slate-100">{product.name}</td>
              <td className="px-4 py-3 font-mono text-xs text-forest-400">{product.sku}</td>
              <td className="px-4 py-3 text-slate-300">{product.category}</td>
              <td className="px-4 py-3 font-semibold text-slate-100">
                ${product.price.toFixed(2)}
              </td>
              <td className="px-4 py-3 font-mono text-slate-200">{product.onHandQuantity}</td>
              <td className="px-4 py-3">
                <InventoryStatusBadge status={product.syncStatus} />
              </td>
              <td className="px-4 py-3">
                <Badge variant={product.status === 'ACTIVE' ? 'success' : 'neutral'}>
                  {product.status}
                </Badge>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
