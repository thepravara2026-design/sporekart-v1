import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CustomerInformation } from '../CustomerInformation';
import { UserProfileDto } from '../../../../services/authApi';

const makeProfile = (): UserProfileDto => ({
  id: 'cust-1',
  email: 'buyer@example.com',
  firstName: 'A.',
  lastName: 'Buyer',
  role: 'CUSTOMER',
  status: 'ACTIVE',
});

describe('CustomerInformation (FD-12)', () => {
  it('renders the authenticated customer name and email', () => {
    render(<CustomerInformation profile={makeProfile()} />);
    expect(screen.getByTestId('checkout-customer-name')).toHaveTextContent('A. Buyer');
    expect(screen.getByTestId('checkout-customer-email')).toHaveTextContent('buyer@example.com');
  });

  it('shows a skeleton while the profile is loading', () => {
    render(<CustomerInformation profile={null} isLoading />);
    expect(screen.queryByText('buyer@example.com')).not.toBeInTheDocument();
    expect(screen.getByTestId('checkout-customer-information')).toBeInTheDocument();
  });

  it('renders placeholders when no profile is available', () => {
    render(<CustomerInformation profile={null} />);
    expect(screen.getByTestId('checkout-customer-name')).toHaveTextContent('—');
    expect(screen.getByTestId('checkout-customer-email')).toHaveTextContent('—');
  });
});