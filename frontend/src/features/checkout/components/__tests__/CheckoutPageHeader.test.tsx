import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CheckoutPageHeader } from '../CheckoutPageHeader';

describe('CheckoutPageHeader (FD-12)', () => {
  it('greets the customer by name when available', () => {
    render(<CheckoutPageHeader customerName="A. Buyer" />);
    expect(screen.getByText(/A\. Buyer, review your delivery details/)).toBeInTheDocument();
  });

  it('falls back to a generic message without a profile', () => {
    render(<CheckoutPageHeader />);
    expect(screen.getByText(/Review your delivery details and place your order securely\./)).toBeInTheDocument();
  });
});