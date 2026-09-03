import { FC, useState } from 'react';
import { GrowerInventoryItem, StockAdjustmentPayload } from '../types/growerInventory';
import { GrowerStockStatus } from './GrowerStockStatus';
import { GrowerStockAdjustDialog } from './GrowerStockAdjustDialog';
import { Button } from '../../../components/ui/Button';

export interface GrowerInventoryTableProps {
  inventory: GrowerInventoryItem[];
  onAdjustStock: (payload: StockAdjustmentPayload) => Promise<void>;
  isAdjusting?: boolean;
}

export const GrowerInventoryTable: FC<GrowerInventoryTableProps> = ({
  inventory,
  onAdjustStock,
  isAdjusting = false,
}) => {
  const [selectedItem, setSelectedItem] = useState<GrowerInventoryItem | null>(null);

  return (
    <div style={{ overflowX: 'auto', borderRadius: '0.5rem', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#0d231a', fontSize: '0.875rem' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.1)', color: '#9ca3af', backgroundColor: 'rgba(0, 0, 0, 0.2)' }}>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>SKU</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Product Name</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>On Hand</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Reserved</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Available</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Stock Health</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600, textAlign: 'right' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {inventory.map((item) => (
            <tr key={item.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)', color: '#e5e7eb' }}>
              <td style={{ padding: '0.75rem 1rem', fontWeight: 600, color: '#10b981' }}>{item.sku}</td>
              <td style={{ padding: '0.75rem 1rem', fontWeight: 500 }}>{item.productName}</td>
              <td style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>{item.onHandQuantity}</td>
              <td style={{ padding: '0.75rem 1rem', color: '#9ca3af' }}>{item.reservedQuantity}</td>
              <td style={{ padding: '0.75rem 1rem', fontWeight: 700, color: '#34d399' }}>{item.availableQuantity}</td>
              <td style={{ padding: '0.75rem 1rem' }}>
                <GrowerStockStatus
                  onHandQuantity={item.onHandQuantity}
                  reservedQuantity={item.reservedQuantity}
                  reorderPoint={item.reorderPoint}
                />
              </td>
              <td style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>
                <Button size="sm" variant="secondary" onClick={() => setSelectedItem(item)}>
                  Adjust Stock
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedItem && (
        <GrowerStockAdjustDialog
          isOpen={Boolean(selectedItem)}
          sku={selectedItem.sku}
          currentOnHand={selectedItem.onHandQuantity}
          onClose={() => setSelectedItem(null)}
          onConfirm={onAdjustStock}
          isLoading={isAdjusting}
        />
      )}
    </div>
  );
};
