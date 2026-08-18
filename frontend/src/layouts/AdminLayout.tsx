import { FC } from 'react';
import { Outlet } from 'react-router-dom';
import { SkipLink } from '../components/layout/SkipLink';
import { Header } from '../components/layout/Header';
import { Sidebar } from '../components/layout/Sidebar';
import { Footer } from '../components/layout/Footer';
import { ADMIN_NAVIGATION } from '../config/navigation';

export const AdminLayout: FC = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <SkipLink targetId="admin-main-content" label="Skip to admin content" />
      <Header />
      <div style={{ display: 'flex', flex: 1 }}>
        <Sidebar title="Admin Console" items={ADMIN_NAVIGATION} ariaLabel="Admin console sidebar navigation" />
        <main id="admin-main-content" tabIndex={-1} style={{ flex: 1, padding: '2rem', outline: 'none' }}>
          <Outlet />
        </main>
      </div>
      <Footer />
    </div>
  );
};
