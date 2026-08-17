import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TrainingProgramManagement } from '../TrainingProgramManagement';
import { axiosInstance } from '../../../../services/apiClient';

vi.mock('../../../../services/apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}));

describe('TrainingProgramManagement Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockPrograms = {
    data: {
      data: {
        content: [
          {
            id: 'prog-1',
            slug: 'mushroom-cultivation-beginner',
            title: 'Mushroom Cultivation Beginner',
            description: 'Learn spawn inoculation and substrate prep',
            category: 'CULTIVATION',
            durationHours: 8,
            status: 'ACTIVE',
            priceAmount: 999.0,
            currency: 'INR',
            createdAt: '2026-08-17T12:00:00Z',
            updatedAt: '2026-08-17T12:00:00Z',
          },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
      },
    },
  };

  it('renders TrainingProgramManagement title and program list table', async () => {
    (axiosInstance.get as any).mockResolvedValue(mockPrograms);

    render(<TrainingProgramManagement />);

    expect(screen.getByText('Training Program Management')).toBeInTheDocument();

    const titleEl = await screen.findByText('Mushroom Cultivation Beginner');
    expect(titleEl).toBeInTheDocument();
    expect(screen.getByText('mushroom-cultivation-beginner')).toBeInTheDocument();
    expect(screen.getAllByText('ACTIVE').length).toBeGreaterThanOrEqual(1);
  });

  it('opens Create Program modal when clicking "+ Create Program"', async () => {
    (axiosInstance.get as any).mockResolvedValue(mockPrograms);

    render(<TrainingProgramManagement />);

    const createBtn = screen.getByRole('button', { name: /\+ Create Program/i });
    fireEvent.click(createBtn);

    expect(screen.getByRole('heading', { name: 'Create Training Program' })).toBeInTheDocument();
  });

  it('handles activation toggle click', async () => {
    (axiosInstance.get as any).mockResolvedValue(mockPrograms);
    (axiosInstance.post as any).mockResolvedValue({
      data: {
        data: { ...mockPrograms.data.data.content[0], status: 'INACTIVE' },
      },
    });

    render(<TrainingProgramManagement />);

    const titleEl = await screen.findByText('Mushroom Cultivation Beginner');
    expect(titleEl).toBeInTheDocument();

    const deactivateBtn = screen.getByRole('button', { name: 'Deactivate' });
    fireEvent.click(deactivateBtn);

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith('/api/v1/admin/training-programs/prog-1/deactivate');
    });
  });
});
