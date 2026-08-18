import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { CheckoutShippingForm } from '../CheckoutShippingForm';
import { AddressDto } from '../../../../services/orderApi';

const renderForm = (props: Partial<React.ComponentProps<typeof CheckoutShippingForm>> = {}) =>
  render(<CheckoutShippingForm onSubmit={vi.fn()} {...props} />);

const fillRequiredFields = () => {
  fireEvent.change(screen.getByLabelText(/Full name/), { target: { value: 'A. Buyer' } });
  fireEvent.change(screen.getByLabelText(/Phone/), { target: { value: '+919876543210' } });
  fireEvent.change(screen.getByLabelText(/Address line 1/), { target: { value: '42 Fungal Lane' } });
  fireEvent.change(screen.getByLabelText(/City/), { target: { value: 'Bengaluru' } });
  fireEvent.change(screen.getByLabelText(/State/), { target: { value: 'Karnataka' } });
  fireEvent.change(screen.getByLabelText(/Postal code/), { target: { value: '560001' } });
};

describe('CheckoutShippingForm (FD-12)', () => {
  it('renders address fields and the continue CTA', () => {
    renderForm();
    expect(screen.getByTestId('checkout-shipping-form')).toBeInTheDocument();
    expect(screen.getByLabelText(/Full name/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Continue to Payment' })).toBeInTheDocument();
  });

  it('submits a complete address', () => {
    const onSubmit = vi.fn();
    renderForm({ onSubmit });
    fillRequiredFields();
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Payment' }));

    expect(onSubmit).toHaveBeenCalledTimes(1);
    const submitted = onSubmit.mock.calls[0][0] as AddressDto;
    expect(submitted.fullName).toBe('A. Buyer');
    expect(submitted.postalCode).toBe('560001');
  });

  it('shows validation errors and does not submit an invalid address', () => {
    const onSubmit = vi.fn();
    renderForm({ onSubmit });

    fireEvent.change(screen.getByLabelText(/Phone/), { target: { value: 'abc' } });
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Payment' }));

    expect(screen.getByText('Full name is required.')).toBeInTheDocument();
    expect(screen.getByText('Enter a valid phone number.')).toBeInTheDocument();
    expect(screen.getByText('City is required.')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('pre-fills initial values for the back-navigation case', async () => {
    const initialValues: AddressDto = {
      fullName: 'A. Buyer',
      phone: '+919876543210',
      addressLine1: '42 Fungal Lane',
      addressLine2: '',
      city: 'Bengaluru',
      state: 'Karnataka',
      postalCode: '560001',
      country: 'India',
    };
    renderForm({ initialValues });

    await waitFor(() => {
      expect(screen.getByLabelText(/Full name/)).toHaveValue('A. Buyer');
      expect(screen.getByLabelText(/City/)).toHaveValue('Bengaluru');
    });
  });

  it('renders the back button only when a handler is provided', () => {
    renderForm();
    expect(screen.queryByRole('button', { name: 'Back' })).not.toBeInTheDocument();

    renderForm({ onCancel: vi.fn() });
    expect(screen.getByRole('button', { name: 'Back' })).toBeInTheDocument();
  });
});