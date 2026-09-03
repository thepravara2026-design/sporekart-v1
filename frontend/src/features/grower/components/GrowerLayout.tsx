import { FC } from 'react';
import { Outlet } from 'react-router-dom';
import { SkipLink } from '../../../components/layout/SkipLink';
import { Header } from '../../../components/layout/Header';
import { Sidebar } from '../../../components/layout/Sidebar';
import { Footer } from '../../../components/layout/Footer';
import { GROWER_NAVIGATION } from '../../../config/navigation';

export const GrowerLayout: FC = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: '#091510', color: '#e5e7eb' }}>
      <SkipLink targetId="grower-main-content" label="Skip to grower portal content" />
      <Header />
      <div style={{ display: 'flex', flex: 1 }}>
        <Sidebar title="Grower Portal" items={GROWER_NAVIGATION} ariaLabel="Grower operational sidebar navigation" />
        <main id="grower-main-content" tabIndex={-1} style={{ flex: 1, padding: '2rem', outline: 'none' }}>
          <Outlet />
        </main>
      </div>
      <Footer />
    </div>
  );
};
