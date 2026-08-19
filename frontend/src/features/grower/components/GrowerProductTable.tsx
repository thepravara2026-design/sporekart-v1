import { FC } from 'react';
import { GrowerProduct } from '../types/growerProduct';
import { GrowerStatusBadge } from './GrowerStatusBadge';
import { formatCurrency, getProductStatusMeta } from '../utils/growerUtils';

export interface GrowerProductTableProps {
  products: GrowerProduct[];
}

export const GrowerProductTable: FC<GrowerProductTableProps> = ({ products }) => {
  return (
    <div style={{ overflowX: 'auto', borderRadius: '0.5rem', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#0d231a', fontSize: '0.875rem' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.1)', color: '#9ca3af', backgroundColor: 'rgba(0, 0, 0, 0.2)' }}>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>SKU</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Product Name</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Category</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Price</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Status</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => {
            const meta = getProductStatusMeta(p.status);
            return (
              <tr key={p.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)', color: '#e5e7eb' }}>
                <td style={{ padding: '0.75rem 1rem', fontWeight: 600, color: '#10b981' }}>{p.sku}</td>
                <td style={{ padding: '0.75rem 1rem', fontWeight: 500 }}>{p.name}</td>
                <td style={{ padding: '0.75rem 1rem', color: '#9ca3af' }}>{p.categoryName || 'Mycology'}</td>
                <td style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>
                  {p.strikeOutPrice != null && p.strikeOutPrice > p.price && (
                    <span style={{ fontSize: '0.8rem', textDecoration: 'line-through', color: '#6b7280', marginRight: '0.35rem', fontWeight: 400 }}>
                      {formatCurrency(p.strikeOutPrice, p.currency)}
                    </span>
                  )}
                  <span>{formatCurrency(p.price, p.currency)}</span>
                </td>
                <td style={{ padding: '0.75rem 1rem' }}>
                  <GrowerStatusBadge label={meta.label} variant={meta.variant} />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};
