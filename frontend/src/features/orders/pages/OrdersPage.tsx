import { FC, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { Alert } from '../../../components/ui/Alert';
import { ORDER_PAGE_SIZE } from '../constants/orderConstants';
import { OrderStatusFilter } from '../constants/orderConstants';
import { useOrders } from '../hooks/useOrder';
import { OrderFilters } from '../components/OrderFilters';
import { OrderList } from '../components/OrderList';
import { OrderPagination } from '../components/OrderPagination';
import { OrderSkeleton } from '../components/OrderSkeleton';
import { OrderErrorState } from '../components/OrderErrorState';
import { getOrderErrorMessage } from '../utils/orderUtils';
import { isAuthenticated } from '../../cart/utils/cartUtils';

/**
 * OrdersPage — the customer's order history. Renders the backend-paginated
 * list (zero-indexed pages, capped size) with an optional status filter.
 */
export const OrdersPage: FC = () => {
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState<OrderStatusFilter>('ALL');
  const authenticated = isAuthenticated();

  const { data, isLoading, isError, error, refetch } = useOrders(page, ORDER_PAGE_SIZE, status, authenticated);

  useEffect(() => {
    setPage(0);
  }, [status]);

  const breadcrumbs = <Breadcrumb items={[{ label: 'Home', path: '/' }, { label: 'My Orders' }]} />;

  if (!authenticated) {
    return (
      <PageShell title="My Orders" breadcrumbs={breadcrumbs}>
        <Alert variant="warning" title="Sign in required">
          Sign in to view your order history.
        </Alert>
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem' }}>
          <Link to="/products" className="btn btn-secondary btn-sm">
            Continue Shopping
          </Link>
        </div>
      </PageShell>
    );
  }

  const pageData = data?.data;

  return (
    <PageShell
      title="My Orders"
      subtitle="Track, manage, and review everything you have ordered."
      breadcrumbs={breadcrumbs}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }} data-testid="orders-page">
        <OrderFilters value={status} onChange={setStatus} />

        {isLoading && <OrderSkeleton />}

        {isError && <OrderErrorState message={getOrderErrorMessage(error)} onRetry={() => refetch()} />}

        {pageData && <OrderList orders={pageData.content} />}

        {pageData && pageData.totalPages > 1 && (
          <OrderPagination currentPage={page} totalPages={pageData.totalPages} onPageChange={setPage} />
        )}
      </div>
    </PageShell>
  );
};