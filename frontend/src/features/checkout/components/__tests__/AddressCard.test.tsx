import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AddressCard } from '../AddressCard';
import { AddressDto } from '../../../../services/orderApi';

const makeAddress = (): AddressDto => ({
  fullName: 'A. Buyer',
  phone: '+919876543210',
  addressLine1: '42 Fungal Lane',
  addressLine2: '',
  city: 'Bengaluru',
  state: 'Karnataka',
  postalCode: '560001',
  country: 'India',
});

describe('AddressCard (FD-12)', () => {
  it('renders the full address summary read-only', () => {
    render(<AddressCard address={makeAddress()} />);
    expect(screen.getByTestId('checkout-address-card')).toBeInTheDocument();
    expect(screen.getByText('A. Buyer')).toBeInTheDocument();
    expect(screen.getByText('42 Fungal Lane')).toBeInTheDocument();
    expect(screen.getByText('Bengaluru, Karnataka 560001')).toBeInTheDocument();
    expect(screen.getByText('India')).toBeInTheDocument();
    expect(screen.getByText('+919876543210')).toBeInTheDocument();
  });

  it('omits an empty optional address line', () => {
    render(<AddressCard address={makeAddress()} />);
    expect(screen.queryByText('Apt 5')).not.toBeInTheDocument();
  });
});
