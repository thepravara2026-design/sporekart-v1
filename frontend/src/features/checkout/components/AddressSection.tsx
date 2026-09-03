import { FC } from 'react';
import { AddressDto } from '../../../services/orderApi';
import { AddressForm } from './AddressForm';

export interface AddressSectionProps {
  initialValues?: AddressDto;
  isSubmitting?: boolean;
  submitLabel?: string;
  onSubmit: (address: AddressDto) => void;
  onCancel?: () => void;
}

/**
 * AddressSection — the delivery address step of checkout. The backend provides
 * no saved address book, so a single inline address form is rendered (select /
 * create / edit / delete flows are not applicable until a backend address API
 * exists).
 */
export const AddressSection: FC<AddressSectionProps> = ({
  initialValues,
  isSubmitting = false,
  submitLabel,
  onSubmit,
  onCancel,
}) => {
  return (
    <div data-testid="checkout-address-section">
      <AddressForm
        initialValues={initialValues}
        isSubmitting={isSubmitting}
        submitLabel={submitLabel}
        onSubmit={onSubmit}
        onCancel={onCancel}
      />
    </div>
  );
};
