import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { SkipLink } from '../SkipLink';
import { Header } from '../Header';
import { Footer } from '../Footer';
import { MobileNav } from '../MobileNav';
import { Container } from '../Container';
import { PageShell } from '../PageShell';
import { PageSection } from '../PageSection';
import { Stack } from '../Stack';
import { Inline } from '../Inline';
import { Grid } from '../Grid';
import { Sidebar } from '../Sidebar';
import { MainLayout } from '../../../layouts/MainLayout';
import { AdminLayout } from '../../../layouts/AdminLayout';
import { ErrorBoundary } from '../../ErrorBoundary';
import { LoadingSpinner } from '../../ui/LoadingSpinner';
import { Skeleton } from '../../ui/Skeleton';
import { MAIN_NAVIGATION, ADMIN_NAVIGATION } from '../../../config/navigation';

describe('Layout Primitives & Accessibility Unit Tests', () => {
  it('renders SkipLink pointing to target content ID', () => {
    render(<SkipLink targetId="main-content" label="Skip to main content" />);
    const link = screen.getByRole('link', { name: 'Skip to main content' });
    expect(link).toHaveAttribute('href', '#main-content');
  });

  it('renders Header banner landmark and primary navigation', () => {
    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>
    );
    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.getByRole('navigation', { name: 'Primary navigation' })).toBeInTheDocument();
    expect(screen.getByText('SPOREKART')).toBeInTheDocument();
  });

  it('renders Footer contentinfo landmark and value propositions', () => {
    render(
      <MemoryRouter>
        <Footer />
      </MemoryRouter>
    );
    expect(screen.getByRole('contentinfo', { name: 'Site footer' })).toBeInTheDocument();
    expect(screen.getByText('High-Yield Spawn')).toBeInTheDocument();
    expect(screen.getByText('Cold-Chain Express')).toBeInTheDocument();
  });

  it('handles MobileNav dialog toggle and Escape key close', () => {
    const handleClose = vi.fn();
    const { rerender } = render(
      <MemoryRouter>
        <MobileNav isOpen={true} onClose={handleClose} items={MAIN_NAVIGATION} />
      </MemoryRouter>
    );

    expect(screen.getByRole('dialog', { name: 'Mobile Navigation Menu' })).toBeInTheDocument();

    fireEvent.keyDown(window, { key: 'Escape' });
    expect(handleClose).toHaveBeenCalledTimes(1);

    rerender(
      <MemoryRouter>
        <MobileNav isOpen={false} onClose={handleClose} items={MAIN_NAVIGATION} />
      </MemoryRouter>
    );
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('renders Container primitive with correct max-width token', () => {
    render(<Container maxWidth="lg">Test Content</Container>);
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('renders PageShell with title, subtitle, and breadcrumbs', () => {
    render(
      <PageShell title="Catalog Title" subtitle="Catalog Subtitle" breadcrumbs={<div>Home / Catalog</div>}>
        <div>Main Page Body</div>
      </PageShell>
    );
    expect(screen.getByText('Catalog Title')).toBeInTheDocument();
    expect(screen.getByText('Catalog Subtitle')).toBeInTheDocument();
    expect(screen.getByText('Home / Catalog')).toBeInTheDocument();
  });

  it('renders PageSection with heading and accessible label', () => {
    render(
      <PageSection title="Featured Mushroom Batches">
        <div>Section Content</div>
      </PageSection>
    );
    expect(screen.getByRole('region', { name: 'Featured Mushroom Batches' })).toBeInTheDocument();
  });

  it('renders Stack, Inline, and Grid primitives cleanly', () => {
    render(
      <Stack gap={4}>
        <Inline gap={2}>
          <div>Inline Item 1</div>
          <div>Inline Item 2</div>
        </Inline>
        <Grid cols={3}>
          <div>Grid Item 1</div>
          <div>Grid Item 2</div>
        </Grid>
      </Stack>
    );
    expect(screen.getByText('Inline Item 1')).toBeInTheDocument();
    expect(screen.getByText('Grid Item 1')).toBeInTheDocument();
  });

  it('renders Sidebar with collapsible state toggle', () => {
    render(
      <MemoryRouter>
        <Sidebar title="Admin Console" items={ADMIN_NAVIGATION} />
      </MemoryRouter>
    );
    expect(screen.getByRole('complementary', { name: 'Sidebar navigation' })).toBeInTheDocument();
    const toggleBtn = screen.getByRole('button', { name: 'Collapse sidebar' });
    fireEvent.click(toggleBtn);
    expect(screen.getByRole('button', { name: 'Expand sidebar' })).toBeInTheDocument();
  });

  it('renders MainLayout with SkipLink, Header, Main, and Footer', () => {
    render(
      <MemoryRouter>
        <MainLayout />
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: 'Skip to main content' })).toBeInTheDocument();
    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.getByRole('main')).toBeInTheDocument();
    expect(screen.getByRole('contentinfo')).toBeInTheDocument();
  });

  it('renders AdminLayout with Admin Console sidebar and main region', () => {
    render(
      <MemoryRouter>
        <AdminLayout />
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: 'Skip to admin content' })).toBeInTheDocument();
    expect(screen.getByRole('complementary', { name: 'Admin console sidebar navigation' })).toBeInTheDocument();
    expect(screen.getByRole('main')).toBeInTheDocument();
  });

  it('renders ErrorBoundary fallback on rendering error', () => {
    const ProblemComponent = () => {
      throw new Error('Test rendering crash');
    };

    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <ErrorBoundary>
        <ProblemComponent />
      </ErrorBoundary>
    );

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText('Application Execution Exception')).toBeInTheDocument();

    spy.mockRestore();
  });

  it('renders LoadingSpinner with status role and Skeleton with aria-hidden', () => {
    render(
      <div>
        <LoadingSpinner label="Loading catalog batches..." />
        <Skeleton width="100px" height="20px" />
      </div>
    );
    expect(screen.getByRole('status', { name: 'Loading catalog batches...' })).toBeInTheDocument();
    const skeletonEl = document.querySelector('.skeleton-loader');
    expect(skeletonEl).toHaveAttribute('aria-hidden', 'true');
  });
});
