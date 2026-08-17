import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BatchManagementConsole } from '../BatchManagementConsole';
import { axiosInstance } from '../../../../services/apiClient';

vi.mock('../../../../services/apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}));

describe('BatchManagementConsole Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockBatches = {
    data: {
      data: {
        content: [
          {
            id: 'batch-1',
            programId: 'prog-123',
            batchCode: 'TRN-2026-001',
            startDate: '2026-09-01T10:00:00Z',
            endDate: '2026-09-05T16:00:00Z',
            totalCapacity: 20,
            occupiedSeats: 5,
            status: 'ACTIVE',
            deliveryMode: 'ONLINE',
            venueInfo: 'Virtual Classroom 1',
            meetingUrl: 'https://meet.google.com/abc-defg-hij',
            timezone: 'Asia/Kolkata',
            schedules: [],
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

  it('renders BatchManagementConsole title and batch table', async () => {
    (axiosInstance.get as any).mockResolvedValue(mockBatches);

    render(<BatchManagementConsole />);

    expect(screen.getByText('Batch & Schedule Management')).toBeInTheDocument();

    const batchCodeEl = await screen.findByText('TRN-2026-001');
    expect(batchCodeEl).toBeInTheDocument();
    expect(screen.getByText('prog-123')).toBeInTheDocument();
    expect(screen.getAllByText('ACTIVE').length).toBeGreaterThanOrEqual(1);
  });

  it('opens Schedule New Batch modal when clicking "+ Schedule New Batch"', async () => {
    (axiosInstance.get as any).mockResolvedValue(mockBatches);

    render(<BatchManagementConsole />);

    const scheduleBtn = screen.getByRole('button', { name: /\+ Schedule New Batch/i });
    fireEvent.click(scheduleBtn);

    expect(screen.getByRole('heading', { name: 'Schedule New Training Batch' })).toBeInTheDocument();
  });

  it('handles activation toggle click for active batch', async () => {
    (axiosInstance.get as any).mockResolvedValue(mockBatches);
    (axiosInstance.post as any).mockResolvedValue({
      data: {
        data: { ...mockBatches.data.data.content[0], status: 'PLANNED' },
      },
    });

    render(<BatchManagementConsole />);

    const batchCodeEl = await screen.findByText('TRN-2026-001');
    expect(batchCodeEl).toBeInTheDocument();

    const deactivateBtn = screen.getByRole('button', { name: 'Deactivate' });
    fireEvent.click(deactivateBtn);

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith('/api/v1/admin/batches/batch-1/deactivate');
    });
  });
});
