import { FC } from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';

export const MainLayout: FC = () => {
  return (
    <>
      <nav className="navbar">
        <Link to="/" className="brand">
          SPOREKART v3.0
        </Link>
        <div className="nav-links">
          <NavLink to="/" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} end>
            Home
          </NavLink>
          <NavLink to="/health" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            System Health
          </NavLink>
        </div>
      </nav>
      <main className="container">
        <Outlet />
      </main>
      <footer>
        <p>&copy; 2026 Sporekart Inc. Modular Monolith Baseline Baseline v3.0 (Sprint 0)</p>
      </footer>
    </>
  );
};
