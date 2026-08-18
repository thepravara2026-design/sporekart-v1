import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ProductQuantity } from '../ProductQuantity';

const renderQuantity = (props: Partial<React.ComponentProps<typeof ProductQuantity>> = {}) => {
  const onChange = vi.fn();
  const view = render(<ProductQuantity quantity={1} onQuantityChange={onChange} {...props} />);
  return { onChange, view };
};

describe('ProductQuantity (FD-10)', () => {
  it('renders decrement/increment buttons and a labelled numeric input', () => {
    renderQuantity({ quantity: 3 });
    expect(screen.getByRole('button', { name: 'Decrease quantity' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Increase quantity' })).toBeInTheDocument();
    const input = screen.getByRole('spinbutton', { name: 'Quantity' });
    expect(input).toHaveValue(3);
    expect(input).toHaveAttribute('min', '1');
    expect(input).toHaveAttribute('max', '50');
  });

  it('increments and decrements via buttons', () => {
    const { onChange } = renderQuantity({ quantity: 2 });
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity' }));
    expect(onChange).toHaveBeenCalledWith(3);
    fireEvent.click(screen.getByRole('button', { name: 'Decrease quantity' }));
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it('disables decrement at min', () => {
    renderQuantity({ quantity: 1 });
    expect(screen.getByRole('button', { name: 'Decrease quantity' })).toBeDisabled();
  });

  it('disables increment at max and still allows decrement', () => {
    const { onChange } = renderQuantity({ quantity: 5, max: 5 });
    expect(screen.getByRole('button', { name: 'Increase quantity' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Decrease quantity' }));
    expect(onChange).toHaveBeenCalledWith(4);
  });

  it('clamps invalid numeric input to valid bounds', () => {
    const { onChange } = renderQuantity({ quantity: 1 });

    fireEvent.change(screen.getByRole('spinbutton', { name: 'Quantity' }), { target: { value: '0' } });
    expect(onChange).toHaveBeenCalledWith(1);

    fireEvent.change(screen.getByRole('spinbutton', { name: 'Quantity' }), { target: { value: '-7' } });
    expect(onChange).toHaveBeenCalledWith(1);

    fireEvent.change(screen.getByRole('spinbutton', { name: 'Quantity' }), { target: { value: '999' } });
    expect(onChange).toHaveBeenCalledWith(50);

    fireEvent.change(screen.getByRole('spinbutton', { name: 'Quantity' }), { target: { value: 'abc' } });
    expect(onChange).toHaveBeenCalledWith(1);

    fireEvent.change(screen.getByRole('spinbutton', { name: 'Quantity' }), { target: { value: '' } });
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it('honours a caller-supplied maximum', () => {
    const { onChange } = renderQuantity({ quantity: 3, max: 3 });
    expect(screen.getByRole('button', { name: 'Increase quantity' })).toBeDisabled();
    fireEvent.change(screen.getByRole('spinbutton', { name: 'Quantity' }), { target: { value: '10' } });
    expect(onChange).toHaveBeenCalledWith(3);
  });

  it('disables all controls when disabled', () => {
    renderQuantity({ quantity: 2, disabled: true });
    expect(screen.getByRole('button', { name: 'Decrease quantity' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Increase quantity' })).toBeDisabled();
    expect(screen.getByRole('spinbutton', { name: 'Quantity' })).toBeDisabled();
  });

  it('provides at least 40px touch targets on the stepper buttons', () => {
    renderQuantity({ quantity: 2 });
    const decBtn = screen.getByRole('button', { name: 'Decrease quantity' });
    const incBtn = screen.getByRole('button', { name: 'Increase quantity' });
    expect(decBtn).toHaveStyle({ width: '40px', height: '40px' });
    expect(incBtn).toHaveStyle({ width: '40px', height: '40px' });
  });

  it('responds to keyboard activation on the stepper buttons', () => {
    const { onChange } = renderQuantity({ quantity: 2 });
    const incBtn = screen.getByRole('button', { name: 'Increase quantity' });
    fireEvent.keyDown(incBtn, { key: 'Enter' });
    fireEvent.click(incBtn);
    expect(onChange).toHaveBeenCalledWith(3);
  });
});