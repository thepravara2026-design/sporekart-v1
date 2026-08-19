import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, renderHook } from '@testing-library/react';
import {
  ProductStickyAction,
  useStickyActionVisibility,
} from '../ProductStickyAction';

const originalMatchMedia = window.matchMedia;
const originalIntersectionObserver = window.IntersectionObserver;

function mockMatchMedia(matches: boolean) {
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches,
    media: query,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    addListener: vi.fn(),
    removeListener: vi.fn(),
    onchange: null,
    dispatchEvent: vi.fn(),
  })) as unknown as typeof window.matchMedia;
}

function mockIntersectionObserver(isIntersecting: boolean) {
  class MockIntersectionObserver {
    static instances: MockIntersectionObserver[] = [];
    callback: IntersectionObserverCallback;
    constructor(callback: IntersectionObserverCallback) {
      this.callback = callback;
      MockIntersectionObserver.instances.push(this);
    }
    observe() {
      this.callback([{ isIntersecting } as IntersectionObserverEntry], this as unknown as IntersectionObserver);
    }
    unobserve() {}
    disconnect() {}
    takeRecords() {
      return [];
    }
    root = null;
    rootMargin = '';
    thresholds = [];
  }
  window.IntersectionObserver = MockIntersectionObserver as unknown as typeof IntersectionObserver;
  return MockIntersectionObserver;
}

describe('ProductStickyAction (FD-10)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    window.matchMedia = originalMatchMedia;
    window.IntersectionObserver = originalIntersectionObserver;
  });

  it('renders nothing when not visible', () => {
    render(
      <ProductStickyAction productName="Lions Mane" visible={false} isPending={false} disabled={false} onAddToCart={vi.fn()} />
    );
    expect(screen.queryByRole('button', { name: 'Add Lions Mane to cart' })).not.toBeInTheDocument();
  });

  it('renders an accessible Add to Cart action with the product name', () => {
    render(
      <ProductStickyAction productName="Lions Mane" visible isPending={false} disabled={false} onAddToCart={vi.fn()} />
    );
    const btn = screen.getByRole('button', { name: 'Add Lions Mane to cart' });
    expect(btn).toBeEnabled();
  });

  it('disables the action for unavailable products', () => {
    render(
      <ProductStickyAction productName="Lions Mane" visible isPending={false} disabled onAddToCart={vi.fn()} />
    );
    expect(screen.getByRole('button', { name: 'Add Lions Mane to cart' })).toBeDisabled();
  });

  it('shows a busy state while a request is pending', () => {
    render(
      <ProductStickyAction productName="Lions Mane" visible isPending disabled={false} onAddToCart={vi.fn()} />
    );
    const btn = screen.getByRole('button', { name: 'Add Lions Mane to cart' });
    expect(btn).toBeDisabled();
    expect(btn).toHaveAttribute('aria-busy', 'true');
    expect(screen.getByLabelText('Loading...')).toBeInTheDocument();
  });

  it('triggers the add-to-cart handler on activation', () => {
    const onAddToCart = vi.fn();
    render(
      <ProductStickyAction productName="Lions Mane" visible isPending={false} disabled={false} onAddToCart={onAddToCart} />
    );
    fireEvent.click(screen.getByRole('button', { name: 'Add Lions Mane to cart' }));
    expect(onAddToCart).toHaveBeenCalledTimes(1);
  });

  it('computes sticky visibility: visible on mobile when the panel is out of view', () => {
    mockMatchMedia(true);
    mockIntersectionObserver(false);
    const { result } = renderHook(() => useStickyActionVisibility({ current: document.createElement('div') }));
    expect(result.current).toBe(true);
  });

  it('computes sticky visibility: hidden on mobile when the panel is in view', () => {
    mockMatchMedia(true);
    mockIntersectionObserver(true);
    const { result } = renderHook(() => useStickyActionVisibility({ current: document.createElement('div') }));
    expect(result.current).toBe(false);
  });

  it('computes sticky visibility: hidden on desktop regardless of panel visibility', () => {
    mockMatchMedia(false);
    mockIntersectionObserver(false);
    const { result } = renderHook(() => useStickyActionVisibility({ current: document.createElement('div') }));
    expect(result.current).toBe(false);
  });
});
