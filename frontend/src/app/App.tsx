import { FC, lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../context/AuthContext';
import { ProtectedRoute } from '../components/auth/ProtectedRoute';
import { RequireRole } from '../components/auth/RequireRole';

import { ErrorBoundary } from '../components/ErrorBoundary';
import { ToastProvider } from '../components/ui/Toast';
import { MainLayout } from '../layouts/MainLayout';
import { HomePage } from '../pages/HomePage';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { UnauthorizedPage } from '../pages/UnauthorizedPage';

import { ProductListPage } from '../features/catalog/pages/ProductListPage';
import { ProductDetailPage } from '../features/catalog/pages/ProductDetailPage';
import { CategoryListPage } from '../features/catalog/pages/CategoryListPage';
import { CartPage } from '../features/cart/pages/CartPage';
import { CheckoutPage } from '../features/checkout/pages/CheckoutPage';
import { OrderConfirmationPage } from '../features/checkout/pages/OrderConfirmationPage';

// Code-split secondary routes using React.lazy & Suspense
const HealthPage = lazy(() => import('../pages/HealthPage').then(m => ({ default: m.HealthPage })));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage').then(m => ({ default: m.NotFoundPage })));
const DesignSystemShowcase = lazy(() => import('../pages/DesignSystemShowcase').then(m => ({ default: m.DesignSystemShowcase })));
const OrdersPage = lazy(() => import('../features/orders/pages/OrdersPage').then(m => ({ default: m.OrdersPage })));
const OrderDetailPage = lazy(() => import('../features/orders/pages/OrderDetailPage').then(m => ({ default: m.OrderDetailPage })));
const ReturnRequestPage = lazy(() => import('../features/returns/pages/ReturnRequestPage').then(m => ({ default: m.ReturnRequestPage })));

// Admin Console Routes (FD-16)
const AdminLayout = lazy(() => import('../features/admin/layouts/AdminLayout').then(m => ({ default: m.AdminLayout })));
const AdminDashboardPage = lazy(() => import('../features/admin/pages/AdminDashboardPage').then(m => ({ default: m.AdminDashboardPage })));
const AdminReturnListPage = lazy(() => import('../features/admin/pages/AdminReturnListPage').then(m => ({ default: m.AdminReturnListPage })));
const TrainingProgramManagement = lazy(() => import('../features/admin/pages/TrainingProgramManagement').then(m => ({ default: m.TrainingProgramManagement })));
const BatchManagementConsole = lazy(() => import('../features/admin/pages/BatchManagementConsole').then(m => ({ default: m.BatchManagementConsole })));
const AdminTrainingOperationsConsole = lazy(() => import('../features/admin/pages/AdminTrainingOperationsConsole').then(m => ({ default: m.AdminTrainingOperationsConsole })));
const TrainingReportingConsole = lazy(() => import('../features/admin/pages/TrainingReportingConsole').then(m => ({ default: m.TrainingReportingConsole })));
const NotificationOperationsConsole = lazy(() => import('../features/admin/pages/NotificationOperationsConsole').then(m => ({ default: m.NotificationOperationsConsole })));

// Trainee Portal Routes (FD-13)
const TrainingLayout = lazy(() => import('../features/trainee/layouts/TrainingLayout').then(m => ({ default: m.TrainingLayout })));
const TraineeTrainingConsole = lazy(() => import('../features/trainee/pages/TraineeTrainingConsole').then(m => ({ default: m.TraineeTrainingConsole })));

// Grower Portal Routes (FD-14)
const GrowerLayout = lazy(() => import('../features/grower/components/GrowerLayout').then(m => ({ default: m.GrowerLayout })));
const GrowerDashboardPage = lazy(() => import('../features/grower/pages/GrowerDashboardPage').then(m => ({ default: m.GrowerDashboardPage })));
const GrowerProfilePage = lazy(() => import('../features/grower/pages/GrowerProfilePage').then(m => ({ default: m.GrowerProfilePage })));
const GrowerProductsPage = lazy(() => import('../features/grower/pages/GrowerProductsPage').then(m => ({ default: m.GrowerProductsPage })));
const GrowerInventoryPage = lazy(() => import('../features/grower/pages/GrowerInventoryPage').then(m => ({ default: m.GrowerInventoryPage })));
const GrowerOrdersPage = lazy(() => import('../features/grower/pages/GrowerOrdersPage').then(m => ({ default: m.GrowerOrdersPage })));
const GrowerShipmentsPage = lazy(() => import('../features/grower/pages/GrowerShipmentsPage').then(m => ({ default: m.GrowerShipmentsPage })));
const GrowerReportsPage = lazy(() => import('../features/grower/pages/GrowerReportsPage').then(m => ({ default: m.GrowerReportsPage })));
const GrowerSettingsPage = lazy(() => import('../features/grower/pages/GrowerSettingsPage').then(m => ({ default: m.GrowerSettingsPage })));

// Seller Portal Routes (FD-15)
const SellerDashboardPage = lazy(() => import('../features/seller/pages/SellerDashboardPage').then(m => ({ default: m.SellerDashboardPage })));
const SellerProductManagementPage = lazy(() => import('../features/seller/pages/SellerProductManagementPage').then(m => ({ default: m.SellerProductManagementPage })));
const SellerInventoryPage = lazy(() => import('../features/seller/pages/SellerInventoryPage').then(m => ({ default: m.SellerInventoryPage })));
const SellerOrderManagementPage = lazy(() => import('../features/seller/pages/SellerOrderManagementPage').then(m => ({ default: m.SellerOrderManagementPage })));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 5,
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
        <AuthProvider>
          <ToastProvider>
            <BrowserRouter>
              <Routes>
                {/* Public Auth Routes */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/unauthorized" element={<UnauthorizedPage />} />

                {/* Master Admin Portal (FD-16) — Protected ROLE_ADMIN */}
                <Route
                  path="admin"
                  element={
                    <ProtectedRoute>
                      <RequireRole roles={['ROLE_ADMIN']}>
                        <Suspense fallback={<RouteFallback />}>
                          <AdminLayout />
                        </Suspense>
                      </RequireRole>
                    </ProtectedRoute>
                  }
                >
                  <Route index element={<AdminDashboardPage />} />
                  <Route path="dashboard" element={<AdminDashboardPage />} />
                  <Route path="returns" element={<AdminReturnListPage />} />
                  <Route path="training" element={<TrainingProgramManagement />} />
                  <Route path="training/batches" element={<BatchManagementConsole />} />
                  <Route path="training/operations" element={<AdminTrainingOperationsConsole />} />
                  <Route path="training/reports" element={<TrainingReportingConsole />} />
                  <Route path="notifications" element={<NotificationOperationsConsole />} />
                </Route>

                {/* Trainee Training Portal (FD-13) — Protected ROLE_TRAINEE / ROLE_GROWER / ROLE_ADMIN */}
                <Route
                  path="training"
                  element={
                    <ProtectedRoute>
                      <RequireRole roles={['ROLE_TRAINEE', 'ROLE_GROWER', 'ROLE_ADMIN']}>
                        <Suspense fallback={<RouteFallback />}>
                          <TrainingLayout />
                        </Suspense>
                      </RequireRole>
                    </ProtectedRoute>
                  }
                >
                  <Route index element={<TraineeTrainingConsole />} />
                  <Route path="console" element={<TraineeTrainingConsole />} />
                </Route>

                {/* Grower Portal (FD-14) — Protected ROLE_GROWER / ROLE_ADMIN */}
                <Route
                  path="grower"
                  element={
                    <ProtectedRoute>
                      <RequireRole roles={['ROLE_GROWER', 'ROLE_ADMIN']}>
                        <Suspense fallback={<RouteFallback />}>
                          <GrowerLayout />
                        </Suspense>
                      </RequireRole>
                    </ProtectedRoute>
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

                {/* Seller Portal (FD-15) — Protected ROLE_SELLER / ROLE_GROWER / ROLE_ADMIN */}
                <Route
                  path="seller"
                  element={
                    <ProtectedRoute>
                      <RequireRole roles={['ROLE_SELLER', 'ROLE_GROWER', 'ROLE_ADMIN']}>
                        <Suspense fallback={<RouteFallback />}>
                          <SellerDashboardPage />
                        </Suspense>
                      </RequireRole>
                    </ProtectedRoute>
                  }
                >
                  <Route index element={<SellerDashboardPage />} />
                  <Route path="dashboard" element={<SellerDashboardPage />} />
                  <Route path="products" element={<Suspense fallback={<RouteFallback />}><SellerProductManagementPage /></Suspense>} />
                  <Route path="inventory" element={<Suspense fallback={<RouteFallback />}><SellerInventoryPage /></Suspense>} />
                  <Route path="orders" element={<Suspense fallback={<RouteFallback />}><SellerOrderManagementPage /></Suspense>} />
                </Route>

                {/* Main Storefront Routes */}
                <Route path="/" element={<MainLayout />}>
                  <Route index element={<HomePage />} />
                  <Route path="products" element={<ProductListPage />} />
                  <Route path="products/:productId" element={<ProductDetailPage />} />
                  <Route path="categories" element={<CategoryListPage />} />

                  {/* Customer Commerce Routes — Protected ROLE_CUSTOMER */}
                  <Route
                    path="cart"
                    element={
                      <ProtectedRoute>
                        <RequireRole roles={['ROLE_CUSTOMER']}>
                          <CartPage />
                        </RequireRole>
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="checkout"
                    element={
                      <ProtectedRoute>
                        <RequireRole roles={['ROLE_CUSTOMER']}>
                          <CheckoutPage />
                        </RequireRole>
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="checkout/confirmation"
                    element={
                      <ProtectedRoute>
                        <RequireRole roles={['ROLE_CUSTOMER']}>
                          <OrderConfirmationPage />
                        </RequireRole>
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="orders"
                    element={
                      <ProtectedRoute>
                        <RequireRole roles={['ROLE_CUSTOMER']}>
                          <Suspense fallback={<RouteFallback />}>
                            <OrdersPage />
                          </Suspense>
                        </RequireRole>
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="orders/:orderReference"
                    element={
                      <ProtectedRoute>
                        <RequireRole roles={['ROLE_CUSTOMER']}>
                          <Suspense fallback={<RouteFallback />}>
                            <OrderDetailPage />
                          </Suspense>
                        </RequireRole>
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="orders/:orderReference/return-request"
                    element={
                      <ProtectedRoute>
                        <RequireRole roles={['ROLE_CUSTOMER']}>
                          <Suspense fallback={<RouteFallback />}>
                            <ReturnRequestPage />
                          </Suspense>
                        </RequireRole>
                      </ProtectedRoute>
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
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
};

export default App;
