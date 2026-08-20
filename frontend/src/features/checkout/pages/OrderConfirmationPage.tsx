import { FC } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { LoadingSpinner } from '../../../components/ui/LoadingSpinner';
import { CheckCircle2, Package, MapPin } from 'lucide-react';
import { useOrderByReference } from '../hooks/useOrder';
import { formatPrice } from '../../catalog/utils/catalogUtils';
import { OrderStatus, getOrderStatusLabel } from '../utils/orderStatus';
import { getCheckoutErrorMessage } from '../utils/checkoutUtils';

/**
 * OrderConfirmationPage — post-payment confirmation. Reads the order number
 * from the query string (set by usePlaceOrder) and loads the server-confirmed
 * order detail so the customer always sees authoritative totals and status.
 */
export const OrderConfirmationPage: FC = () => {
  const [searchParams] = useSearchParams();
  const orderNumber = searchParams.get('orderNumber') ?? '';

  const { data: orderResponse, isLoading, isError, error } = useOrderByReference(orderNumber);

  const breadcrumbs = (
    <Breadcrumb items={[{ label: 'Home', path: '/' }, { label: 'Order Confirmation' }]} />
  );

  if (!orderNumber) {
    return (
      <PageShell title="Order Confirmation" breadcrumbs={breadcrumbs}>
        <Alert variant="warning" title="No order found">
          We could not find the order you are looking for. Check your order history or continue shopping.
        </Alert>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem', marginTop: '1rem' }}>
          <Link to="/products" className="btn btn-primary btn-sm">
            Continue Shopping
          </Link>
        </div>
      </PageShell>
    );
  }

  if (isLoading) {
    return (
      <PageShell title="Order Confirmation" breadcrumbs={breadcrumbs}>
        <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
          <LoadingSpinner label="Loading your order..." />
        </div>
      </PageShell>
    );
  }

  if (isError) {
    return (
      <PageShell title="Order Confirmation" breadcrumbs={breadcrumbs}>
        <Alert variant="error" title="Unable to load your order">
          {error ? getCheckoutErrorMessage(error) : 'Something went wrong while loading your order.'}
        </Alert>
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem' }}>
          <Link to="/products" className="btn btn-secondary btn-sm">
            Continue Shopping
          </Link>
        </div>
      </PageShell>
    );
  }

  const order = orderResponse?.data;
  if (!order) return null;

  const currency = order.currency || 'INR';
  const confirmed = order.status === OrderStatus.CONFIRMED || order.status === OrderStatus.PAID;

  return (
    <PageShell title="Order Confirmation" breadcrumbs={breadcrumbs} className="order-confirmation-page">
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }} data-testid="order-confirmation">
        <Card data-testid="order-confirmation-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <CheckCircle2 size={40} style={{ color: confirmed ? '#10b981' : '#f59e0b' }} aria-hidden="true" />
            <div>
              <h2 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                {confirmed ? 'Thank you — your order is confirmed' : 'Order received'}
              </h2>
              <p style={{ margin: '0.25rem 0 0', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                Order number <strong data-testid="order-confirmation-number">{order.orderNumber}</strong>
              </p>
            </div>
          </div>
        </Card>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem', alignItems: 'start' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <Card>
              <CardHeader>
                <CardTitle>Order Status</CardTitle>
              </CardHeader>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.95rem' }}>
                <span
                  data-testid="order-confirmation-status"
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.4rem',
                    padding: '0.3rem 0.75rem',
                    borderRadius: 'var(--radius-full)',
                    fontSize: '0.85rem',
                    fontWeight: 700,
                    color: confirmed ? '#10b981' : '#f59e0b',
                    border: `1px solid ${confirmed ? 'rgba(16,185,129,0.4)' : 'rgba(245,158,11,0.4)'}`,
                  }}
                >
                  <Package size={14} aria-hidden="true" />
                  {getOrderStatusLabel(order.status)}
                </span>
              </div>
            </Card>

            {order.shippingAddress && (
              <Card>
                <CardHeader>
                  <CardTitle>Shipping Address</CardTitle>
                </CardHeader>
                <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }} data-testid="order-confirmation-address">
                  <MapPin size={16} style={{ marginTop: '0.15rem', flexShrink: 0 }} aria-hidden="true" />
                  <div>
                    <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{order.shippingAddress.fullName}</div>
                    <div>{order.shippingAddress.addressLine1}</div>
                    {order.shippingAddress.addressLine2 && <div>{order.shippingAddress.addressLine2}</div>}
                    <div>
                      {order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}
                    </div>
                    {order.shippingAddress.country && <div>{order.shippingAddress.country}</div>}
                    <div style={{ marginTop: '0.25rem' }}>{order.shippingAddress.phone}</div>
                  </div>
                </div>
              </Card>
            )}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <Card data-testid="order-confirmation-totals">
              <CardHeader>
                <CardTitle>Order Summary</CardTitle>
              </CardHeader>
              <dl style={{ margin: 0, display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.9rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <dt style={{ color: 'var(--text-secondary)' }}>Items</dt>
                  <dd style={{ margin: 0, fontWeight: 600 }}>
                    {order.items.reduce((sum, item) => sum + item.quantity, 0)}
                  </dd>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <dt style={{ color: 'var(--text-secondary)' }}>Subtotal</dt>
                  <dd style={{ margin: 0, fontWeight: 600 }}>{formatPrice(order.subtotal, currency)}</dd>
                </div>
                {order.discountTotal > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <dt style={{ color: 'var(--text-secondary)' }}>Discount</dt>
                    <dd style={{ margin: 0, fontWeight: 600, color: '#10b981' }}>
                      −{formatPrice(order.discountTotal, currency)}
                    </dd>
                  </div>
                )}
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <dt style={{ color: 'var(--text-secondary)' }}>Tax</dt>
                  <dd style={{ margin: 0, fontWeight: 600 }}>{formatPrice(order.taxTotal, currency)}</dd>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <dt style={{ color: 'var(--text-secondary)' }}>Shipping</dt>
                  <dd style={{ margin: 0, fontWeight: 600 }}>{formatPrice(order.shippingFee, currency)}</dd>
                </div>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    fontSize: '1.05rem',
                    borderTop: '1px solid var(--border-color)',
                    paddingTop: '0.75rem',
                  }}
                >
                  <dt style={{ fontWeight: 800 }}>Total paid</dt>
                  <dd style={{ margin: 0, fontWeight: 800, color: 'var(--accent-primary)' }} data-testid="order-confirmation-total">
                    {formatPrice(order.grandTotal, currency)}
                  </dd>
                </div>
              </dl>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Items</CardTitle>
              </CardHeader>
              <ul style={{ listStyle: 'none', margin: 0, padding: 0, display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {order.items.map((item) => (
                  <li key={item.id} data-testid="order-confirmation-item" style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', fontSize: '0.9rem' }}>
                    <span style={{ minWidth: 0 }}>
                      <span style={{ fontWeight: 600, color: 'var(--text-primary)', display: 'block' }}>
                        {item.productNameSnapshot}
                      </span>
                      <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                        Qty {item.quantity} × {formatPrice(item.unitPrice, currency)}
                      </span>
                    </span>
                    <span style={{ fontWeight: 600, whiteSpace: 'nowrap' }}>
                      {formatPrice(item.lineTotal, currency)}
                    </span>
                  </li>
                ))}
              </ul>
            </Card>

            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
              <Link to={`/orders/${order.orderNumber}`}>
                <Button variant="primary" size="lg">
                  View Order Details
                </Button>
              </Link>
              <Link to="/orders">
                <Button variant="secondary" size="lg">
                  My Orders
                </Button>
              </Link>
              <Link to="/products">
                <Button variant="outline" size="lg">
                  Continue Shopping
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </PageShell>
  );
};