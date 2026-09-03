import { FC } from 'react';
import { Outlet } from 'react-router-dom';
import { SkipLink } from '../components/layout/SkipLink';
import { Header } from '../components/layout/Header';
import { Sidebar } from '../components/layout/Sidebar';
import { Footer } from '../components/layout/Footer';
import { ACCOUNT_NAVIGATION } from '../config/navigation';

export const AuthenticatedLayout: FC = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <SkipLink targetId="account-main-content" label="Skip to account content" />
      <Header />
      <div style={{ display: 'flex', flex: 1 }}>
        <Sidebar title="My Portal" items={ACCOUNT_NAVIGATION} ariaLabel="Customer account sidebar navigation" />
        <main id="account-main-content" tabIndex={-1} style={{ flex: 1, padding: '2rem', outline: 'none' }}>
          <Outlet />
        </main>
      </div>
      <Footer />
    </div>
  );
};
