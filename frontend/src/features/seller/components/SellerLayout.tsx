import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Header } from '../../../components/layout/Header';
import { Footer } from '../../../components/layout/Footer';
import { Container } from '../../../components/layout/Container';

export interface SellerLayoutProps {
  children: React.ReactNode;
}

export const SellerLayout: React.FC<SellerLayoutProps> = ({ children }) => {
  const location = useLocation();

  const navItems = [
    { label: 'Seller Dashboard', path: '/seller/dashboard' },
    { label: 'Product Manager', path: '/seller/products' },
    { label: 'Inventory Sync', path: '/seller/inventory' },
    { label: 'Order Fulfillment', path: '/seller/orders' },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100 antialiased selection:bg-forest-500 selection:text-white">
      <Header />

      {/* Seller Portal Header Banner */}
      <div className="border-b border-slate-800 bg-slate-900/60 backdrop-blur-md">
        <Container>
          <div className="py-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-forest-950 border border-forest-600/50 text-forest-400">
                  Seller Portal
                </span>
                <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-white">
                  Sporekart Marketplace Seller
                </h1>
              </div>
              <p className="text-xs text-slate-400 mt-1">
                Manage products, inventory sync status, sales metrics, and payouts.
              </p>
            </div>

            {/* Navigation Tabs */}
            <nav className="flex items-center gap-1 overflow-x-auto pb-1 sm:pb-0 scrollbar-none">
              {navItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                      isActive
                        ? 'bg-forest-600 text-white shadow-sm'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                    }`}
                  >
                    {item.label}
                  </Link>
                );
              })}
            </nav>
          </div>
        </Container>
      </div>

      {/* Main Content Area */}
      <main className="flex-1 py-8">
        <Container>{children}</Container>
      </main>

      <Footer />
    </div>
  );
};
