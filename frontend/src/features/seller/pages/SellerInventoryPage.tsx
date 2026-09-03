import React, { useState } from 'react';
import { SellerLayout } from '../components/SellerLayout';
import { SellerInventoryTable } from '../components/SellerInventoryTable';
import { useAdjustSellerStock, useSellerInventory } from '../hooks/useSeller';
import { SellerInventoryItem } from '../types/seller';
import { Dialog } from '../../../components/ui/Dialog';
import { Input } from '../../../components/ui/Input';
import { Button } from '../../../components/ui/Button';

export const SellerInventoryPage: React.FC = () => {
  const { data: inventory, isLoading } = useSellerInventory();
  const adjustStockMutation = useAdjustSellerStock();

  const [selectedItem, setSelectedItem] = useState<SellerInventoryItem | null>(null);
  const [newQuantity, setNewQuantity] = useState<number>(0);
  const [reason, setReason] = useState('Routine Warehouse Audit');

  const handleOpenAdjust = (item: SellerInventoryItem) => {
    setSelectedItem(item);
    setNewQuantity(item.onHandQuantity);
  };

  const handleConfirmAdjust = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedItem) return;

    adjustStockMutation.mutate(
      {
        sku: selectedItem.sku,
        newOnHandQuantity: newQuantity,
        reason,
      },
      {
        onSuccess: () => {
          setSelectedItem(null);
        },
      }
    );
  };

  return (
    <SellerLayout>
      <div data-testid="seller-inventory-page" className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-100 tracking-tight">Inventory Sync & Stock Control</h2>
          <p className="text-sm text-slate-400">
            Real-time warehouse stock allocation, reserved quantities, and inventory sync status.
          </p>
        </div>

        {/* Inventory Table */}
        <SellerInventoryTable
          inventory={inventory || []}
          isLoading={isLoading}
          onAdjustStock={handleOpenAdjust}
        />

        {/* Adjust Stock Dialog */}
        <Dialog
          isOpen={!!selectedItem}
          onClose={() => setSelectedItem(null)}
          title={`Adjust Stock for ${selectedItem?.sku || ''}`}
        >
          <form onSubmit={handleConfirmAdjust} className="space-y-4 py-2">
            <div>
              <p className="text-xs text-slate-400">Product: <span className="text-slate-200 font-semibold">{selectedItem?.productName}</span></p>
              <p className="text-xs text-slate-400">Current On-Hand: <span className="text-slate-200 font-semibold">{selectedItem?.onHandQuantity} units</span></p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">New On-Hand Quantity</label>
              <Input
                type="number"
                value={newQuantity}
                onChange={(e) => setNewQuantity(parseInt(e.target.value, 10) || 0)}
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Adjustment Reason</label>
              <Input
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="e.g. Physical inventory count correction"
                required
              />
            </div>

            <div className="flex justify-end gap-2 pt-4">
              <Button type="button" variant="outline" onClick={() => setSelectedItem(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={adjustStockMutation.isPending}>
                {adjustStockMutation.isPending ? 'Updating...' : 'Confirm Stock Adjustment'}
              </Button>
            </div>
          </form>
        </Dialog>
      </div>
    </SellerLayout>
  );
};
