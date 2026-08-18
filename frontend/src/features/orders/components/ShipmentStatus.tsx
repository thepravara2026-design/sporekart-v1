import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { Badge } from '../../../components/ui/Badge';
import { ShipmentDto } from '../../../services/shippingApi';
import { getShipmentStatusLabel, getShipmentStatusVariant, formatOrderDate } from '../utils/orderUtils';

export interface ShipmentStatusProps {
  shipment: ShipmentDto;
}

const Field: FC<{ label: string; value?: string | null }> = ({ label, value }) =>
  value ? (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem' }}>
      <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.03em', color: 'var(--text-secondary)', fontWeight: 600 }}>
        {label}
      </span>
      <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-primary)' }}>{value}</span>
    </div>
  ) : null;

/** Shipment details card — AWB, tracking number, courier, ETA. */
export const ShipmentStatus: FC<ShipmentStatusProps> = ({ shipment }) => (
  <Card data-testid="shipment-status">
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>Shipment</h3>
      <Badge variant={getShipmentStatusVariant(shipment.status)}>{getShipmentStatusLabel(shipment.status)}</Badge>
    </div>

    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem' }}>
      <Field label="Reference" value={shipment.shipmentReference} />
      <Field label="Courier" value={shipment.courierName} />
      <Field label="Tracking Number" value={shipment.trackingNumber || shipment.awb} />
      <Field label="AWB" value={shipment.awb} />
      {shipment.estimatedDeliveryAt && (
        <Field label="Estimated Delivery" value={formatOrderDate(shipment.estimatedDeliveryAt)} />
      )}
      {shipment.deliveredAt && <Field label="Delivered" value={formatOrderDate(shipment.deliveredAt)} />}
    </div>
  </Card>
);