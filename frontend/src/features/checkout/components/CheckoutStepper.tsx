import { FC } from 'react';
import { Check } from 'lucide-react';
import { CHECKOUT_STEP_LABELS, CheckoutStepIndex } from '../constants/checkoutConstants';

export interface CheckoutStepperProps {
  currentStep: CheckoutStepIndex;
}

/**
 * CheckoutStepper — accessible multi-step progress indicator for the checkout
 * flow. Completed steps render a check and the current step is announced via
 * aria-current. Steps are purely informational; navigation happens via the
 * form CTAs below the stepper.
 */
export const CheckoutStepper: FC<CheckoutStepperProps> = ({ currentStep }) => {
  return (
    <nav aria-label="Checkout progress" className="checkout-stepper" data-testid="checkout-stepper">
      <ol
        style={{
          listStyle: 'none',
          display: 'flex',
          alignItems: 'center',
          gap: '0.25rem',
          margin: 0,
          padding: 0,
          flexWrap: 'wrap',
        }}
      >
        {CHECKOUT_STEP_LABELS.map((label, index) => {
          const isComplete = index < currentStep;
          const isCurrent = index === currentStep;

          return (
            <li
              key={label}
              style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}
            >
              {index > 0 && (
                <span
                  aria-hidden="true"
                  style={{
                    width: '2rem',
                    height: '2px',
                    margin: '0 0.35rem',
                    borderRadius: 'var(--radius-full)',
                    backgroundColor: isComplete ? 'var(--accent-primary)' : 'var(--border-color)',
                    transition: 'background-color var(--transition-normal)',
                  }}
                />
              )}
              <span
                data-testid={`checkout-step-${index}`}
                aria-current={isCurrent ? 'step' : undefined}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  padding: '0.4rem 0.9rem',
                  borderRadius: 'var(--radius-full)',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  color: isCurrent
                    ? '#ffffff'
                    : isComplete
                      ? 'var(--accent-primary)'
                      : 'var(--text-muted)',
                  backgroundColor: isCurrent ? 'var(--accent-primary)' : isComplete ? 'rgba(16, 185, 129, 0.1)' : 'transparent',
                  border: isCurrent ? '1px solid var(--accent-primary)' : isComplete ? '1px solid rgba(16, 185, 129, 0.4)' : '1px solid var(--border-color)',
                  boxShadow: isCurrent ? '0 0 12px 0 rgba(16, 185, 129, 0.3)' : 'none',
                  transition: 'all var(--transition-normal)',
                }}
              >
                {isComplete && <Check size={14} aria-hidden="true" />}
                {label}
              </span>
            </li>
          );
        })}
      </ol>
    </nav>
  );
};