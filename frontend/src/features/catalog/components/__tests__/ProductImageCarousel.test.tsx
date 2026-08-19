import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { ProductImageCarousel } from '../ProductImageCarousel';
import { PRODUCT_IMAGE_CAROUSEL_INTERVAL } from '../../constants/catalogConstants';

describe('ProductImageCarousel', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders a branded placeholder when no images are provided', () => {
    render(<ProductImageCarousel images={[]} alt="Blue Oyster Spawn" />);
    expect(screen.getByText('Mushroom Spawn')).toBeInTheDocument();
  });

  it('renders a static single image without carousel controls', () => {
    render(<ProductImageCarousel images={['https://img/a.jpg']} alt="Blue Oyster Spawn" />);
    const img = screen.getByAltText('Blue Oyster Spawn');
    expect(img).toHaveAttribute('src', 'https://img/a.jpg');
    expect(screen.queryByRole('button', { name: /Next product image/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Previous product image/i })).not.toBeInTheDocument();
  });

  it('renders multi-image carousel with indicators and navigation controls', () => {
    const images = ['https://img/a.jpg', 'https://img/b.jpg', 'https://img/c.jpg'];
    render(<ProductImageCarousel images={images} alt="Blue Oyster Spawn" />);

    expect(screen.getByRole('button', { name: 'Next product image' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Previous product image' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View image 1 of 3' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View image 2 of 3' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View image 3 of 3' })).toBeInTheDocument();
  });

  it('marks the first indicator as active initially', () => {
    render(<ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />);
    expect(screen.getByRole('button', { name: 'View image 1 of 2' })).toHaveAttribute('aria-current', 'true');
    expect(screen.getByRole('button', { name: 'View image 2 of 2' })).not.toHaveAttribute('aria-current');
  });

  it('advances to the next image when the next button is clicked', () => {
    const images = ['https://img/a.jpg', 'https://img/b.jpg'];
    render(<ProductImageCarousel images={images} alt="Spawn" />);

    fireEvent.click(screen.getByRole('button', { name: 'Next product image' }));
    expect(screen.getByRole('button', { name: 'View image 2 of 2' })).toHaveAttribute('aria-current', 'true');
    expect(screen.getByText('Viewing image 2 of 2')).toBeInTheDocument();
  });

  it('wraps around to the first image after navigating past the last one', () => {
    render(<ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />);

    fireEvent.click(screen.getByRole('button', { name: 'Previous product image' }));
    expect(screen.getByRole('button', { name: 'View image 2 of 2' })).toHaveAttribute('aria-current', 'true');

    fireEvent.click(screen.getByRole('button', { name: 'Next product image' }));
    expect(screen.getByRole('button', { name: 'View image 1 of 2' })).toHaveAttribute('aria-current', 'true');
  });

  it('selects an image directly via indicator click', () => {
    const images = ['https://img/a.jpg', 'https://img/b.jpg', 'https://img/c.jpg'];
    render(<ProductImageCarousel images={images} alt="Spawn" />);

    fireEvent.click(screen.getByRole('button', { name: 'View image 3 of 3' }));
    expect(screen.getByRole('button', { name: 'View image 3 of 3' })).toHaveAttribute('aria-current', 'true');
    expect(screen.getByText('Viewing image 3 of 3')).toBeInTheDocument();
  });

  it('auto-advances after the configured interval', () => {
    render(<ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />);

    expect(screen.getByText('Viewing image 1 of 2')).toBeInTheDocument();

    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL);
    });

    expect(screen.getByText('Viewing image 2 of 2')).toBeInTheDocument();
  });

  it('pauses auto-advance while hovered', () => {
    render(<ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />);
    const carousel = screen.getByRole('region');

    fireEvent.mouseEnter(carousel);
    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL * 3);
    });
    expect(screen.getByText('Viewing image 1 of 2')).toBeInTheDocument();

    fireEvent.mouseLeave(carousel);
    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL);
    });
    expect(screen.getByText('Viewing image 2 of 2')).toBeInTheDocument();
  });

  it('pauses auto-advance while focused and resumes after blur', () => {
    render(<ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />);
    const carousel = screen.getByRole('region');

    fireEvent.focus(carousel);
    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL * 3);
    });
    expect(screen.getByText('Viewing image 1 of 2')).toBeInTheDocument();

    fireEvent.blur(carousel);
    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL);
    });
    expect(screen.getByText('Viewing image 2 of 2')).toBeInTheDocument();
  });

  it('does not auto-advance when reduced motion is preferred', () => {
    const matchMedia = vi.fn().mockImplementation((query: string) => ({
      matches: query.includes('prefers-reduced-motion'),
      media: query,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      addListener: vi.fn(),
      removeListener: vi.fn(),
      dispatchEvent: vi.fn(),
    }));
    vi.stubGlobal('matchMedia', matchMedia);

    render(<ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />);

    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL * 3);
    });
    expect(screen.getByText('Viewing image 1 of 2')).toBeInTheDocument();

    vi.unstubAllGlobals();
  });

  it('does not auto-advance when autoPlay is disabled', () => {
    render(<ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" autoPlay={false} />);

    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL * 3);
    });
    expect(screen.getByText('Viewing image 1 of 2')).toBeInTheDocument();
  });

  it('cleans up its timer on unmount', () => {
    const { unmount } = render(
      <ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />
    );

    unmount();
    act(() => {
      vi.advanceTimersByTime(PRODUCT_IMAGE_CAROUSEL_INTERVAL * 3);
    });
    expect(screen.queryByText('Viewing image 2 of 2')).not.toBeInTheDocument();
  });

  it('does not render controls when only a single image exists after filtering blanks', () => {
    render(<ProductImageCarousel images={['', '  ', 'https://img/a.jpg']} alt="Spawn" />);
    expect(screen.getByAltText('Spawn')).toHaveAttribute('src', 'https://img/a.jpg');
    expect(screen.queryByRole('button', { name: /Next product image/i })).not.toBeInTheDocument();
  });

  it('stops control clicks from bubbling to the surrounding link', () => {
    const handleClick = vi.fn();
    render(
      <div onClick={handleClick}>
        <ProductImageCarousel images={['https://img/a.jpg', 'https://img/b.jpg']} alt="Spawn" />
      </div>
    );

    fireEvent.click(screen.getByRole('button', { name: 'Next product image' }));
    expect(handleClick).not.toHaveBeenCalled();
  });
});
