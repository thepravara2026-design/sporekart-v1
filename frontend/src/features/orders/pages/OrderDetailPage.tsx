import { FC } from 'react';
import { Link, useParams } from 'react-router-dom';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { Card } from '../../../components/ui/Card';
import { Alert } from '../../../components/ui/Alert';
import { LoadingSpinner } from '../../../components/ui/LoadingSpinner';
import { MapPin } from 'lucide-react';
import { useOrderByReference, useOrderTimeline } from '../hooks/useOrder';
import { useOrderShipment, useShipmentTracking } from '../hooks/useShipment';
import { OrderStatusBadge } from '../components/OrderStatusBadge';
import { OrderItems } from '../components/OrderItems';
import { OrderSummary } from '../components/OrderSummary';
import { OrderTimeline } from '../components/OrderTimeline';
import { ShipmentStatus } from '../components/ShipmentStatus';
import { ShipmentTracking } from '../components/ShipmentTracking';
import { OrderActions } from '../components/OrderActions';
import { OrderReturnHandoff, OrderSupportHandoff } from '../components/OrderReturnHandoff';
import { getOrderErrorMessage, formatOrderDate } from '../utils/orderUtils';
import { isAuthenticated } from '../../cart/utils/cartUtils';

/** Order statuses for which a shipment record is expected to exist. */
const SHIPMENT_RELEVANT_STATUSES = new Set(['SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'COMPLETED']);

/**
 * OrderDetailPage — full order detail: items, totals, timeline, shipment &
 * tracking, cancellation, and return/support handoffs.
 */
export const OrderDetailPage: FC = () => {
  const { orderReference = '' } = useParams<{ orderReference: string }>();
  const authenticated = isAuthenticated();

  const { data: orderResponse, isLoading, isError, error } = useOrderByReference(orderReference, authenticated);
  const { data: timelineResponse } = useOrderTimeline(orderReference, authenticated);

  const order = orderResponse?.data;
  const showShipment = Boolean(order && SHIPMENT_RELEVANT_STATUSES.has(order.status));
  const { data: shipment } = useOrderShipment(orderReference, showShipment && authenticated);
  const { data: tracking } = useShipmentTracking(orderReference, showShipment && authenticated);

  const breadcrumbs = (
    <Breadcrumb items={[{ label: 'Home', path: '/' }, { label: 'My Orders', path: '/orders' }, { label: orderReference }]} />
  );

  if (!authenticated) {
    return (
      <PageShell title="Order Details" breadcrumbs={breadcrumbs}>
        <Alert variant="warning" title="Sign in required">
          Sign in to view order details.
        </Alert>
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem' }}>
          <Link to="/orders" className="btn btn-secondary btn-sm">
            Back to My Orders
          </Link>
        </div>
      </PageShell>
    );
  }

  if (isLoading) {
    return (
      <PageShell title="Order Details" breadcrumbs={breadcrumbs}>
        <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
          <LoadingSpinner label="Loading your order..." />
        </div>
      </PageShell>
    );
  }

  if (isError || !order) {
    return (
      <PageShell title="Order Details" breadcrumbs={breadcrumbs}>
        <Alert variant="error" title="Unable to load this order">
          {error ? getOrderErrorMessage(error) : 'Something went wrong while loading this order.'}
        </Alert>
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem' }}>
          <Link to="/orders" className="btn btn-secondary btn-sm">
            Back to My Orders
          </Link>
        </div>
      </PageShell>
    );
  }

  return (
    <PageShell title={`Order ${order.orderNumber}`} breadcrumbs={breadcrumbs}>
      <div data-testid="order-detail-page" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
        <Card data-testid="order-detail-header" style={{ background: 'var(--bg-surface-elevated)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-xl)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Placed {formatOrderDate(order.createdAt)}
              </span>
              <OrderStatusBadge status={order.status} />
            </div>
            <OrderActions order={order} />
          </div>
        </Card>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.25rem', alignItems: 'start' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <OrderItems order={order} />
            {timelineResponse?.data && <OrderTimeline timeline={timelineResponse.data} />}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <OrderSummary order={order} />

            {order.shippingAddress && (
              <Card data-testid="order-detail-address">
                <h3 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>Shipping Address</h3>
                <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
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

            {showShipment && shipment && <ShipmentStatus shipment={shipment} />}
            {showShipment && tracking && <ShipmentTracking tracking={tracking} />}

            <OrderReturnHandoff orderReference={orderReference} />
            <OrderSupportHandoff orderReference={orderReference} />
          </div>
        </div>
      </div>
    </PageShell>
  );
};