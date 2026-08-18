import { FC, useState, FormEvent } from 'react';
import { AddressDto } from '../../../services/orderApi';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { FormField } from '../../../components/ui/FormField';
import { Input } from '../../../components/ui/Input';
import { Button } from '../../../components/ui/Button';
import { ArrowRight } from 'lucide-react';

export interface CheckoutShippingFormProps {
  initialValues?: AddressDto;
  isSubmitting?: boolean;
  onSubmit: (address: AddressDto) => void;
  onCancel?: () => void;
}

type AddressErrors = Partial<Record<keyof AddressDto, string>>;

const EMPTY_ADDRESS: AddressDto = {
  fullName: '',
  phone: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  country: 'India',
};

const validateAddress = (address: AddressDto): AddressErrors => {
  const errors: AddressErrors = {};
  if (!address.fullName.trim()) errors.fullName = 'Full name is required.';
  if (!address.phone.trim()) errors.phone = 'Phone number is required.';
  else if (!/^[+]?[\d\s-]{7,15}$/.test(address.phone.trim())) {
    errors.phone = 'Enter a valid phone number.';
  }
  if (!address.addressLine1.trim()) errors.addressLine1 = 'Address line 1 is required.';
  if (!address.city.trim()) errors.city = 'City is required.';
  if (!address.state.trim()) errors.state = 'State is required.';
  if (!address.postalCode.trim()) errors.postalCode = 'Postal code is required.';
  else if (!/^[A-Za-z0-9-]{3,10}$/.test(address.postalCode.trim())) {
    errors.postalCode = 'Enter a valid postal code.';
  }
  return errors;
};

/**
 * CheckoutShippingForm — collects the shipping address that is persisted as
 * the order's AddressSnapshot. Validation is client-side convenience; the
 * backend remains the source of truth for address acceptance.
 */
export const CheckoutShippingForm: FC<CheckoutShippingFormProps> = ({
  initialValues,
  isSubmitting = false,
  onSubmit,
  onCancel,
}) => {
  const [address, setAddress] = useState<AddressDto>(initialValues ?? EMPTY_ADDRESS);
  const [errors, setErrors] = useState<AddressErrors>({});

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const nextErrors = validateAddress(address);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length === 0) {
      onSubmit(address);
    }
  };

  const setField = (field: keyof AddressDto, value: string) => {
    setAddress((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  const inputProps = (field: keyof AddressDto) => ({
    value: address[field] ?? '',
    invalid: Boolean(errors[field]),
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => setField(field, e.target.value),
    'aria-describedby': errors[field] ? `${field}-error` : undefined,
  });

  return (
    <form onSubmit={handleSubmit} noValidate data-testid="checkout-shipping-form">
      <Card>
        <CardHeader>
          <CardTitle>Shipping Address</CardTitle>
        </CardHeader>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
          <FormField label="Full name" required htmlFor="fullName" error={errors.fullName}>
            <Input id="fullName" name="fullName" autoComplete="name" placeholder="First and last name" {...inputProps('fullName')} />
          </FormField>

          <FormField label="Phone" required htmlFor="phone" error={errors.phone}>
            <Input id="phone" name="phone" type="tel" autoComplete="tel" placeholder="+91 98765 43210" {...inputProps('phone')} />
          </FormField>

          <FormField label="Address line 1" required htmlFor="addressLine1" error={errors.addressLine1}>
            <Input id="addressLine1" name="addressLine1" autoComplete="address-line1" placeholder="Street address, P.O. box" {...inputProps('addressLine1')} />
          </FormField>

          <FormField label="Address line 2" htmlFor="addressLine2" error={errors.addressLine2}>
            <Input id="addressLine2" name="addressLine2" autoComplete="address-line2" placeholder="Apartment, suite, unit (optional)" {...inputProps('addressLine2')} />
          </FormField>

          <FormField label="City" required htmlFor="city" error={errors.city}>
            <Input id="city" name="city" autoComplete="address-level2" placeholder="City" {...inputProps('city')} />
          </FormField>

          <FormField label="State" required htmlFor="state" error={errors.state}>
            <Input id="state" name="state" autoComplete="address-level1" placeholder="State / Province" {...inputProps('state')} />
          </FormField>

          <FormField label="Postal code" required htmlFor="postalCode" error={errors.postalCode}>
            <Input id="postalCode" name="postalCode" autoComplete="postal-code" placeholder="560001" {...inputProps('postalCode')} />
          </FormField>

          <FormField label="Country" htmlFor="country" error={errors.country}>
            <Input id="country" name="country" autoComplete="country-name" placeholder="India" {...inputProps('country')} />
          </FormField>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          {onCancel && (
            <Button type="button" variant="secondary" onClick={onCancel}>
              Back
            </Button>
          )}
          <Button type="submit" isLoading={isSubmitting} rightIcon={<ArrowRight size={16} />}>
            Continue to Payment
          </Button>
        </div>
      </Card>
    </form>
  );
};