import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { Card } from '../../../components/ui/Card';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { FormField } from '../../../components/ui/FormField';
import { Select } from '../../../components/ui/Select';
import { Input } from '../../../components/ui/Input';
import { Textarea } from '../../../components/ui/Textarea';
import { returnApi, ReturnEligibilityDto } from '../../../services/returnApi';
import { getOrderErrorMessage } from '../../orders/utils/orderUtils';

const RETURN_REASON_OPTIONS = [
  { value: 'DAMAGED', label: 'Damaged on arrival' },
  { value: 'DEFECTIVE', label: 'Defective / Malfunctioning' },
  { value: 'WRONG_ITEM', label: 'Wrong item received' },
  { value: 'WRONG_SIZE', label: 'Wrong size received' },
  { value: 'QUALITY_ISSUE', label: 'Quality issue' },
  { value: 'MISSING_PART', label: 'Missing part' },
  { value: 'NOT_AS_EXPECTED', label: 'Item not as expected' },
  { value: 'CUSTOMER_CHANGED_MIND', label: 'Changed mind (unopened)' },
  { value: 'OTHER', label: 'Other' },
];

export interface ReturnRequestPageProps {
  orderReference?: string;
  onReturnSubmitted?: (returnRef: string) => void;
}

export const ReturnRequestPage: React.FC<ReturnRequestPageProps> = ({
  orderReference: orderReferenceProp,
  onReturnSubmitted
}) => {
  const { orderReference: orderReferenceParam } = useParams<{ orderReference: string }>();
  const orderReference = orderReferenceProp ?? orderReferenceParam ?? '';

  const [eligibility, setEligibility] = useState<ReturnEligibilityDto | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [reasonCode, setReasonCode] = useState<string>('DAMAGED');
  const [reasonDescription, setReasonDescription] = useState<string>('');
  const [evidenceUrls, setEvidenceUrls] = useState<string>('');
  const [selectedQuantities, setSelectedQuantities] = useState<Record<string, number>>({});
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [submittedReturnRef, setSubmittedReturnRef] = useState<string | null>(null);

  useEffect(() => {
    fetchEligibility();
  }, [orderReference]);

  const fetchEligibility = async () => {
    if (!orderReference) return;
    setLoading(true);
    setError(null);
    try {
      const data = await returnApi.checkEligibility(orderReference);
      setEligibility(data);
      const initialQty: Record<string, number> = {};
      data.items.forEach(item => {
        if (item.isReturnable) {
          initialQty[item.orderItemId] = item.returnableQuantity;
        }
      });
      setSelectedQuantities(initialQty);
    } catch (err: unknown) {
      setError(getOrderErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleQuantityChange = (orderItemId: string, qty: number) => {
    setSelectedQuantities(prev => ({
      ...prev,
      [orderItemId]: Math.max(0, qty)
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!eligibility || !eligibility.eligible) return;

    const returnItems = Object.entries(selectedQuantities)
      .filter(([, qty]) => qty > 0)
      .map(([orderItemId, qty]) => ({
        orderItemId,
        quantity: qty,
      }));

    if (returnItems.length === 0) {
      setError('Please select at least one item to return.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const result = await returnApi.createReturn(orderReference, {
        reasonCode,
        reasonDescription: reasonDescription.trim() || undefined,
        evidenceUrls: evidenceUrls.trim() || undefined,
        items: returnItems
      });

      setSubmittedReturnRef(result.returnReference);
      if (onReturnSubmitted) {
        onReturnSubmitted(result.returnReference);
      }
    } catch (err: unknown) {
      setError(getOrderErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const breadcrumbs = (
    <Breadcrumb items={[
      { label: 'Home', path: '/' },
      { label: 'My Orders', path: '/orders' },
      { label: `Order ${orderReference}`, path: `/orders/${orderReference}` },
      { label: 'Request Return' },
    ]} />
  );

  if (!orderReference) {
    return (
      <PageShell title="Request Return" breadcrumbs={breadcrumbs}>
        <Alert variant="warning" title="Order not specified">
          No order was provided for this return request.
        </Alert>
      </PageShell>
    );
  }

  if (loading) {
    return (
      <PageShell title="Request Return" breadcrumbs={breadcrumbs}>
        <div className="p-6 text-center text-gray-600">Evaluating return eligibility for Order #{orderReference}...</div>
      </PageShell>
    );
  }

  if (error && !eligibility) {
    return (
      <PageShell title="Request Return" breadcrumbs={breadcrumbs}>
        <Alert variant="error" title="Unable to check eligibility">
          {error}
        </Alert>
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem' }}>
          <Link to={`/orders/${orderReference}`} className="btn btn-secondary btn-sm">
            Back to Order
          </Link>
        </div>
      </PageShell>
    );
  }

  return (
    <PageShell
      title={`Request Return — Order #${orderReference}`}
      subtitle="Review eligibility and submit your return request."
      breadcrumbs={breadcrumbs}
    >
      <div data-testid="return-request-page" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
        {submittedReturnRef ? (
          <Card>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800, margin: 0, color: 'var(--text-primary)' }}>
              Return request submitted
            </h2>
            <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
              Your return reference is <strong>{submittedReturnRef}</strong>. Our team will review your request.
            </p>
            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <Link to={`/orders/${orderReference}`}>
                <Button variant="secondary" size="sm">Back to Order</Button>
              </Link>
              <Link to="/orders">
                <Button variant="primary" size="sm">My Orders</Button>
              </Link>
            </div>
          </Card>
        ) : eligibility && !eligibility.eligible ? (
          <Alert variant="warning" title="This order is not eligible for return">
            {eligibility.ineligibilityReason || 'The return window or eligibility criteria are not met for this order.'}
          </Alert>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <Alert variant="error" title="Unable to submit return">
                {error}
              </Alert>
            )}

            <Card>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <FormField label="Return Reason" htmlFor="return-reason" required>
                  <Select
                    id="return-reason"
                    value={reasonCode}
                    options={RETURN_REASON_OPTIONS}
                    onChange={e => setReasonCode(e.target.value)}
                  />
                </FormField>

                <FormField label="Description / Notes" htmlFor="return-notes" required>
                  <Textarea
                    id="return-notes"
                    rows={3}
                    value={reasonDescription}
                    onChange={e => setReasonDescription(e.target.value)}
                    placeholder="Provide specific details regarding why you are returning these items..."
                  />
                </FormField>

                <FormField label="Photo / Video Evidence URLs (optional)" htmlFor="return-evidence">
                  <Input
                    id="return-evidence"
                    type="text"
                    value={evidenceUrls}
                    onChange={e => setEvidenceUrls(e.target.value)}
                    placeholder="https://example.com/photo1.jpg"
                  />
                </FormField>
              </div>
            </Card>

            <Card>
              <h2 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                Select Items to Return
              </h2>
              <div data-testid="return-eligibility-items" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {eligibility?.items.map(item => (
                  <div key={item.orderItemId} data-testid="return-eligibility-item" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem', padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)' }}>
                    <div>
                      <p style={{ margin: 0, fontWeight: 600, color: 'var(--text-primary)' }}>{item.productName}</p>
                      <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        SKU: {item.sku}
                      </p>
                      <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        Max returnable: {item.returnableQuantity} of {item.orderedQuantity}
                      </p>
                    </div>
                    {item.isReturnable ? (
                      <label className="flex items-center gap-2" style={{ fontSize: '0.875rem' }}>
                        Qty:
                        <Input
                          type="number"
                          min={0}
                          max={item.returnableQuantity}
                          value={selectedQuantities[item.orderItemId] || 0}
                          onChange={e => handleQuantityChange(item.orderItemId, parseInt(e.target.value) || 0)}
                          style={{ width: '80px' }}
                        />
                      </label>
                    ) : (
                      <span style={{ fontSize: '0.8rem', color: 'var(--danger-color)', fontWeight: 600 }}>
                        {item.reasonCode || 'Not returnable'}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </Card>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <Link to={`/orders/${orderReference}`}>
                <Button variant="secondary" size="md">Cancel</Button>
              </Link>
              <Button
                type="submit"
                size="md"
                isLoading={submitting}
                data-testid="submit-return-button"
              >
                Submit Return Request
              </Button>
            </div>
          </form>
        )}
      </div>
    </PageShell>
  );
};