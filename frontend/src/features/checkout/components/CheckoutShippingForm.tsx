/**
 * Backward-compatible alias. The address form is now the canonical AddressForm
 * component; CheckoutShippingForm is kept so existing imports and tests
 * continue to work.
 */
export { AddressForm as CheckoutShippingForm } from './AddressForm';
export type { AddressFormProps as CheckoutShippingFormProps } from './AddressForm';
