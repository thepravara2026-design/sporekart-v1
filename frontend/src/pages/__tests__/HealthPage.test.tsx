import { render, screen, waitFor } from '@testing-library/react';
import { HealthPage } from '../HealthPage';
import { apiClient } from '../../services/apiClient';
import { describe, it, expect, vi } from 'vitest';

vi.mock('../../services/apiClient');

describe('HealthPage Component', () => {
  it('displays OPERATIONAL status when API call succeeds', async () => {
    vi.mocked(apiClient.getHealth).mockResolvedValue({
      success: true,
      data: { status: 'UP' },
    });
    vi.mocked(apiClient.getVersion).mockResolvedValue({
      success: true,
      data: { appName: 'sporekart-backend', version: '0.1.0-SNAPSHOT' },
    });

    render(<HealthPage />);

    await waitFor(() => {
      expect(screen.getByText('OPERATIONAL')).toBeInTheDocument();
      expect(screen.getByText('Status: UP')).toBeInTheDocument();
      expect(screen.getByText(/sporekart-backend/i)).toBeInTheDocument();
    });
  });

  it('displays UNAVAILABLE status when API fails', async () => {
    vi.mocked(apiClient.getHealth).mockRejectedValue(new Error('Network error'));

    render(<HealthPage />);

    await waitFor(() => {
      expect(screen.getByText('UNAVAILABLE')).toBeInTheDocument();
      expect(screen.getByText('Service Offline')).toBeInTheDocument();
    });
  });
});
