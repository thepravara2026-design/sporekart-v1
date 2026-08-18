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

// Code-split secondary routes using React.lazy & Suspense
const HealthPage = lazy(() => import('../pages/HealthPage').then(module => ({ default: module.HealthPage })));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage').then(module => ({ default: module.NotFoundPage })));
const DesignSystemShowcase = lazy(() => import('../pages/DesignSystemShowcase').then(module => ({ default: module.DesignSystemShowcase })));

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
              <Route path="/" element={<MainLayout />}>
                <Route index element={<HomePage />} />
                <Route path="products" element={<ProductListPage />} />
                <Route path="products/:productId" element={<ProductDetailPage />} />
                <Route path="categories" element={<CategoryListPage />} />
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
