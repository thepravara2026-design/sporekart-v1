import { FC } from 'react';
import { MapPin } from 'lucide-react';
import { AddressDto } from '../../../services/orderApi';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';

export interface AddressCardProps {
  address: AddressDto;
}

/**
 * AddressCard — read-only display of the selected delivery address used in the
 * order review step.
 */
export const AddressCard: FC<AddressCardProps> = ({ address }) => {
  return (
    <Card data-testid="checkout-address-card">
      <CardHeader>
        <CardTitle>Delivery Address</CardTitle>
      </CardHeader>
      <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
        <MapPin size={16} style={{ marginTop: '0.15rem', flexShrink: 0 }} aria-hidden="true" />
        <div>
          <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{address.fullName}</div>
          <div>{address.addressLine1}</div>
          {address.addressLine2 && <div>{address.addressLine2}</div>}
          <div>
            {address.city}, {address.state} {address.postalCode}
          </div>
          {address.country && <div>{address.country}</div>}
          <div style={{ marginTop: '0.25rem' }}>{address.phone}</div>
        </div>
      </div>
    </Card>
  );
};
