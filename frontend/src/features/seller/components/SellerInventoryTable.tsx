import React from 'react';
import { Button } from '../../../components/ui/Button';
import { InventoryStatusBadge } from './InventoryStatusBadge';
import { SellerInventoryItem } from '../types/seller';

export interface SellerInventoryTableProps {
  inventory: SellerInventoryItem[];
  isLoading?: boolean;
  onAdjustStock?: (item: SellerInventoryItem) => void;
  testId?: string;
}

export const SellerInventoryTable: React.FC<SellerInventoryTableProps> = ({
  inventory,
  isLoading = false,
  onAdjustStock,
  testId = 'seller-inventory-table',
}) => {
  if (isLoading) {
    return (
      <div data-testid={`${testId}-loading`} className="p-8 text-center text-slate-400">
        Loading seller inventory...
      </div>
    );
  }

  if (!inventory || inventory.length === 0) {
    return (
      <div data-testid={`${testId}-empty`} className="p-8 text-center text-slate-400">
        No inventory items tracked.
      </div>
    );
  }

  return (
    <div data-testid={testId} className="overflow-x-auto rounded-lg border border-slate-800 bg-slate-900/40">
      <table className="w-full text-left text-sm text-slate-200">
        <thead className="bg-slate-800/80 text-xs uppercase tracking-wider text-slate-400 border-b border-slate-800">
          <tr>
            <th className="px-4 py-3 font-semibold">SKU</th>
            <th className="px-4 py-3 font-semibold">Product Name</th>
            <th className="px-4 py-3 font-semibold">Location</th>
            <th className="px-4 py-3 font-semibold">On-Hand</th>
            <th className="px-4 py-3 font-semibold">Reserved</th>
            <th className="px-4 py-3 font-semibold">Available</th>
            <th className="px-4 py-3 font-semibold">Sync Status</th>
            <th className="px-4 py-3 font-semibold">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800/60">
          {inventory.map((item) => (
            <tr key={item.id} className="hover:bg-slate-800/30 transition-colors">
              <td className="px-4 py-3 font-mono text-xs font-medium text-forest-400">{item.sku}</td>
              <td className="px-4 py-3 text-slate-100">{item.productName}</td>
              <td className="px-4 py-3 text-xs text-slate-400">{item.warehouseLocation}</td>
              <td className="px-4 py-3 font-mono font-semibold text-slate-100">{item.onHandQuantity}</td>
              <td className="px-4 py-3 font-mono text-amber-400">{item.reservedQuantity}</td>
              <td className="px-4 py-3 font-mono font-semibold text-emerald-400">{item.availableQuantity}</td>
              <td className="px-4 py-3">
                <InventoryStatusBadge status={item.syncStatus} />
              </td>
              <td className="px-4 py-3">
                {onAdjustStock && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onAdjustStock(item)}
                    className="text-xs border-slate-700 text-slate-200 hover:bg-slate-800"
                  >
                    Adjust Stock
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
