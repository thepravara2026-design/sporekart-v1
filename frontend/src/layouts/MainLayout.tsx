import { FC } from 'react';
import { Outlet } from 'react-router-dom';
import { SkipLink } from '../components/layout/SkipLink';
import { Header } from '../components/layout/Header';
import { Footer } from '../components/layout/Footer';

export const MainLayout: FC = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <SkipLink targetId="main-content" />
      <Header />
      <main id="main-content" tabIndex={-1} style={{ flex: 1, outline: 'none' }}>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};
