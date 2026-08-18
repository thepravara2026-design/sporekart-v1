import { FC, lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ErrorBoundary } from '../components/ErrorBoundary';
import { ToastProvider } from '../components/ui/Toast';
import { MainLayout } from '../layouts/MainLayout';
import { HomePage } from '../pages/HomePage';

import { ProductListPage } from '../features/catalog/pages/ProductListPage';
import { ProductDetailPage } from '../features/catalog/pages/ProductDetailPage';
import { CategoryListPage } from '../features/catalog/pages/CategoryListPage';
import { CartPage } from '../features/cart/pages/CartPage';
import { CheckoutPage } from '../features/checkout/pages/CheckoutPage';
import { OrderConfirmationPage } from '../features/checkout/pages/OrderConfirmationPage';

// Code-split secondary routes using React.lazy & Suspense
const HealthPage = lazy(() => import('../pages/HealthPage').then(module => ({ default: module.HealthPage })));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage').then(module => ({ default: module.NotFoundPage })));
const DesignSystemShowcase = lazy(() => import('../pages/DesignSystemShowcase').then(module => ({ default: module.DesignSystemShowcase })));
const OrdersPage = lazy(() => import('../features/orders/pages/OrdersPage').then(module => ({ default: module.OrdersPage })));
const OrderDetailPage = lazy(() => import('../features/orders/pages/OrderDetailPage').then(module => ({ default: module.OrderDetailPage })));
const ReturnRequestPage = lazy(() => import('../features/returns/pages/ReturnRequestPage').then(module => ({ default: module.ReturnRequestPage })));

// Grower Portal Routes (FD-14)
const GrowerLayout = lazy(() => import('../features/grower/components/GrowerLayout').then(module => ({ default: module.GrowerLayout })));
const GrowerDashboardPage = lazy(() => import('../features/grower/pages/GrowerDashboardPage').then(module => ({ default: module.GrowerDashboardPage })));
const GrowerProfilePage = lazy(() => import('../features/grower/pages/GrowerProfilePage').then(module => ({ default: module.GrowerProfilePage })));
const GrowerProductsPage = lazy(() => import('../features/grower/pages/GrowerProductsPage').then(module => ({ default: module.GrowerProductsPage })));
const GrowerInventoryPage = lazy(() => import('../features/grower/pages/GrowerInventoryPage').then(module => ({ default: module.GrowerInventoryPage })));
const GrowerOrdersPage = lazy(() => import('../features/grower/pages/GrowerOrdersPage').then(module => ({ default: module.GrowerOrdersPage })));
const GrowerShipmentsPage = lazy(() => import('../features/grower/pages/GrowerShipmentsPage').then(module => ({ default: module.GrowerShipmentsPage })));
const GrowerReportsPage = lazy(() => import('../features/grower/pages/GrowerReportsPage').then(module => ({ default: module.GrowerReportsPage })));
const GrowerSettingsPage = lazy(() => import('../features/grower/pages/GrowerSettingsPage').then(module => ({ default: module.GrowerSettingsPage })));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 5, // 5 minutes stale time for catalog data caching
    },
  },
});

const RouteFallback: FC = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '3rem' }}>
    <div style={{ fontSize: '1rem', color: '#9ca3af' }}>Loading page...</div>
  </div>
);

export const App: FC = () => {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <BrowserRouter>
            <Routes>
              {/* Grower Portal (FD-14) */}
              <Route
                path="grower"
                element={
                  <Suspense fallback={<RouteFallback />}>
                    <GrowerLayout />
                  </Suspense>
                }
              >
                <Route index element={<GrowerDashboardPage />} />
                <Route path="profile" element={<GrowerProfilePage />} />
                <Route path="products" element={<GrowerProductsPage />} />
                <Route path="inventory" element={<GrowerInventoryPage />} />
                <Route path="orders" element={<GrowerOrdersPage />} />
                <Route path="shipments" element={<GrowerShipmentsPage />} />
                <Route path="reports" element={<GrowerReportsPage />} />
                <Route path="settings" element={<GrowerSettingsPage />} />
              </Route>

              {/* Main Storefront Routes */}
              <Route path="/" element={<MainLayout />}>
                <Route index element={<HomePage />} />
                <Route path="products" element={<ProductListPage />} />
                <Route path="products/:productId" element={<ProductDetailPage />} />
                <Route path="categories" element={<CategoryListPage />} />
                <Route path="cart" element={<CartPage />} />
                <Route path="checkout" element={<CheckoutPage />} />
                <Route path="checkout/confirmation" element={<OrderConfirmationPage />} />
                <Route
                  path="orders"
                  element={
                    <Suspense fallback={<RouteFallback />}>
                      <OrdersPage />
                    </Suspense>
                  }
                />
                <Route
                  path="orders/:orderReference"
                  element={
                    <Suspense fallback={<RouteFallback />}>
                      <OrderDetailPage />
                    </Suspense>
                  }
                />
                <Route
                  path="orders/:orderReference/return-request"
                  element={
                    <Suspense fallback={<RouteFallback />}>
                      <ReturnRequestPage />
                    </Suspense>
                  }
                />
                <Route
                  path="health"
                  element={
                    <Suspense fallback={<RouteFallback />}>
                      <HealthPage />
                    </Suspense>
                  }
                />
                <Route
                  path="design-system-showcase"
                  element={
                    <Suspense fallback={<RouteFallback />}>
                      <DesignSystemShowcase />
                    </Suspense>
                  }
                />
                <Route
                  path="*"
                  element={
                    <Suspense fallback={<RouteFallback />}>
                      <NotFoundPage />
                    </Suspense>
                  }
                />
              </Route>
            </Routes>
          </BrowserRouter>
        </ToastProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
};

export default App;
