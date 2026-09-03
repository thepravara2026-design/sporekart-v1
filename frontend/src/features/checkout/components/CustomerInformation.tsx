import { FC } from 'react';
import { UserProfileDto } from '../../../services/authApi';
import { Card, CardHeader, CardTitle, CardDescription } from '../../../components/ui/Card';
import { Skeleton } from '../../../components/ui/Skeleton';

export interface CustomerInformationProps {
  profile?: UserProfileDto | null;
  isLoading?: boolean;
}

/**
 * CustomerInformation — read-only display of the authenticated customer's
 * identity used to prefill checkout. The backend exposes no editable customer
 * profile, so this is display-only; order identity always comes from the
 * backend `/auth/me` response.
 */
export const CustomerInformation: FC<CustomerInformationProps> = ({ profile, isLoading = false }) => {
  const fullName = profile ? [profile.firstName, profile.lastName].filter(Boolean).join(' ') : '';

  return (
    <Card data-testid="checkout-customer-information">
      <CardHeader>
        <CardTitle>Customer Information</CardTitle>
        <CardDescription>Order details and receipts are sent to your registered account.</CardDescription>
      </CardHeader>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.95rem' }}>
        {isLoading ? (
          <>
            <Skeleton height="1.1rem" width="60%" />
            <Skeleton height="1.1rem" width="45%" />
          </>
        ) : (
          <>
            <div>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', display: 'block' }}>Name</span>
              <span data-testid="checkout-customer-name" style={{ fontWeight: 600 }}>
                {fullName || '—'}
              </span>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', display: 'block' }}>Email</span>
              <span data-testid="checkout-customer-email" style={{ fontWeight: 600 }}>
                {profile?.email || '—'}
              </span>
            </div>
          </>
        )}
      </div>
    </Card>
  );
};
