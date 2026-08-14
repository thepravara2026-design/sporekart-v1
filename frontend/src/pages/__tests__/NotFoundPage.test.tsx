import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { NotFoundPage } from '../NotFoundPage';
import { describe, it, expect } from 'vitest';

describe('NotFoundPage Component', () => {
  it('renders 404 error page cleanly', () => {
    render(
      <BrowserRouter>
        <NotFoundPage />
      </BrowserRouter>
    );

    expect(screen.getByText('404')).toBeInTheDocument();
    expect(screen.getByText('Page Not Found')).toBeInTheDocument();
    expect(screen.getByText('Return to Home')).toBeInTheDocument();
  });
});
