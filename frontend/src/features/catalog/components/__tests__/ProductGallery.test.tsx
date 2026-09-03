import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ProductGallery } from '../ProductGallery';

const images = ['https://img.test/one.jpg', 'https://img.test/two.jpg', 'https://img.test/three.jpg'];

describe('ProductGallery (FD-10)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the branded placeholder when no images are provided', () => {
    render(<ProductGallery productName="Lions Mane" />);
    expect(screen.getByText('Mushroom Spawn')).toBeInTheDocument();
  });

  it('renders a single image without gallery controls', () => {
    render(<ProductGallery productName="Lions Mane" images={[images[0]]} />);
    expect(screen.getByAltText('Lions Mane image 1')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Previous product image' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Next product image' })).not.toBeInTheDocument();
    expect(screen.queryByRole('group', { name: 'Product images' })).not.toBeInTheDocument();
  });

  it('loads the primary image eagerly and thumbnails lazily', () => {
    render(<ProductGallery productName="Lions Mane" images={images} />);
    const mainImg = screen.getByAltText('Lions Mane image 1');
    expect(mainImg).toHaveAttribute('loading', 'eager');
    const thumbs = screen.getAllByAltText(/thumbnail/);
    expect(thumbs).toHaveLength(3);
    thumbs.forEach((thumb) => expect(thumb).toHaveAttribute('loading', 'lazy'));
  });

  it('renders thumbnails, previous/next controls, and updates the main image on selection', () => {
    render(<ProductGallery productName="Lions Mane" images={images} />);
    expect(screen.getByRole('button', { name: 'Previous product image' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Next product image' })).toBeInTheDocument();

    const thumb2 = screen.getByRole('button', { name: 'View Lions Mane image 2' });
    expect(thumb2).toHaveAttribute('aria-pressed', 'false');
    fireEvent.click(thumb2);
    expect(screen.getByAltText('Lions Mane image 2')).toBeInTheDocument();
    expect(thumb2).toHaveAttribute('aria-pressed', 'true');
  });

  it('supports arrow-key navigation across thumbnails', () => {
    render(<ProductGallery productName="Lions Mane" images={images} />);
    const group = screen.getByRole('group', { name: 'Product images' });
    fireEvent.keyDown(group, { key: 'ArrowRight' });
    expect(screen.getByAltText('Lions Mane image 2')).toBeInTheDocument();
    fireEvent.keyDown(group, { key: 'ArrowRight' });
    expect(screen.getByAltText('Lions Mane image 3')).toBeInTheDocument();
    fireEvent.keyDown(group, { key: 'ArrowLeft' });
    expect(screen.getByAltText('Lions Mane image 2')).toBeInTheDocument();
    fireEvent.keyDown(group, { key: 'Home' });
    expect(screen.getByAltText('Lions Mane image 1')).toBeInTheDocument();
    fireEvent.keyDown(group, { key: 'End' });
    expect(screen.getByAltText('Lions Mane image 3')).toBeInTheDocument();
  });

  it('wraps around when next/previous is used at the boundaries', () => {
    render(<ProductGallery productName="Lions Mane" images={images} />);
    const nextBtn = screen.getByRole('button', { name: 'Next product image' });
    fireEvent.click(nextBtn);
    fireEvent.click(nextBtn);
    fireEvent.click(nextBtn);
    expect(screen.getByAltText('Lions Mane image 1')).toBeInTheDocument();

    const prevBtn = screen.getByRole('button', { name: 'Previous product image' });
    fireEvent.click(prevBtn);
    expect(screen.getByAltText('Lions Mane image 3')).toBeInTheDocument();
  });

  it('opens a lightbox when the zoom control is activated', () => {
    render(<ProductGallery productName="Lions Mane" images={images} />);
    fireEvent.click(screen.getByRole('button', { name: /zoom in on/i }));
    expect(screen.getByRole('dialog', { name: 'Lions Mane - Image View' })).toBeInTheDocument();
    expect(screen.getByAltText('Lions Mane enlarged view')).toBeInTheDocument();
  });

  it('falls back to the placeholder when the primary image fails to load', () => {
    render(<ProductGallery productName="Lions Mane" images={[images[0]]} />);
    fireEvent.error(screen.getByAltText('Lions Mane image 1'));
    expect(screen.getByText('Mushroom Spawn')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /zoom in on/i })).not.toBeInTheDocument();
  });

  it('exposes accessible names for all gallery controls', () => {
    render(<ProductGallery productName="Lions Mane" images={images} />);
    expect(screen.getByRole('button', { name: 'Previous product image' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Next product image' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View Lions Mane image 1' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View Lions Mane image 2' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View Lions Mane image 3' })).toBeInTheDocument();
  });
});
