import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { HomePage } from '../HomePage';
import { describe, it, expect } from 'vitest';

describe('HomePage Component', () => {
  it('renders application identity and tech stack', () => {
    render(
      <BrowserRouter>
        <HomePage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Sporekart Platform v3.0/i)).toBeInTheDocument();
    expect(screen.getByText(/Modular Monolith/i)).toBeInTheDocument();
    expect(screen.getByText(/View Live Health Status/i)).toBeInTheDocument();
  });
});
