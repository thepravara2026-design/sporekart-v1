import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TrainingReportingConsole } from '../TrainingReportingConsole';
import { axiosInstance } from '../../../../services/apiClient';

vi.mock('../../../../services/apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('TrainingReportingConsole Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockOverview = {
    data: {
      data: {
        activeProgramsCount: 5,
        totalBatches: 10,
        activeBatches: 8,
        fullBatches: 2,
        totalCapacitySeats: 200,
        totalOccupiedSeats: 150,
        totalAvailableSeats: 50,
        seatUtilizationPercentage: 75.0,
        totalEnrollments: 160,
        confirmedEnrollments: 140,
        pendingPaymentEnrollments: 10,
        cancelledEnrollments: 10,
        completedEnrollments: 80,
        grossRevenue: 50000,
        totalRefunds: 2000,
        netRevenue: 48000,
        certificatesIssuedCount: 75,
      },
    },
  };

  const mockBatches = {
    data: {
      data: [
        {
          batchId: 'b-1',
          programId: 'prog-1',
          batchCode: 'TRN-2026-001',
          status: 'ACTIVE',
          totalSeats: 20,
          occupiedSeats: 15,
          availableSeats: 5,
          utilizationPercentage: 75.0,
        },
      ],
    },
  };

  const mockAuditLogs = {
    data: {
      data: [
        {
          id: 'aud-1',
          enrollmentId: 'enr-100',
          fromStatus: 'PENDING',
          toStatus: 'CONFIRMED',
          reason: 'Payment verified',
          actor: 'admin@sporekart.com',
          timestamp: '2026-08-17T12:00:00Z',
        },
      ],
    },
  };

  const mockExceptions = {
    data: {
      data: [
        {
          exceptionId: 'exc-1',
          type: 'FAILED_NOTIFICATION',
          description: 'Notification email bounce',
          severity: 'MEDIUM',
          resourceId: 'notif-99',
          resourceType: 'NOTIFICATION',
        },
      ],
    },
  };

  it('renders TrainingReportingConsole title, KPI cards, and exception queue', async () => {
    (axiosInstance.get as any).mockImplementation((url: string) => {
      if (url.includes('/reports/overview')) return Promise.resolve(mockOverview);
      if (url.includes('/reports/batches')) return Promise.resolve(mockBatches);
      if (url.includes('/reports/audit')) return Promise.resolve(mockAuditLogs);
      if (url.includes('/reports/exceptions')) return Promise.resolve(mockExceptions);
      return Promise.reject(new Error('Unknown endpoint'));
    });

    render(<TrainingReportingConsole />);

    expect(await screen.findByText('Training Operations & Audit Console')).toBeInTheDocument();
    expect(screen.getAllByText(/75/)[0]).toBeInTheDocument();
    expect(screen.getByText(/48000/)).toBeInTheDocument();
    expect(screen.getByText('TRN-2026-001')).toBeInTheDocument();
    expect(screen.getByText('Notification email bounce')).toBeInTheDocument();
  });

  it('handles notification retry action', async () => {
    (axiosInstance.get as any).mockImplementation((url: string) => {
      if (url.includes('/reports/overview')) return Promise.resolve(mockOverview);
      if (url.includes('/reports/batches')) return Promise.resolve(mockBatches);
      if (url.includes('/reports/audit')) return Promise.resolve(mockAuditLogs);
      if (url.includes('/reports/exceptions')) return Promise.resolve(mockExceptions);
      return Promise.reject(new Error('Unknown endpoint'));
    });

    (axiosInstance.post as any).mockResolvedValue({ data: { data: true } });

    render(<TrainingReportingConsole />);

    const retryBtn = await screen.findByRole('button', { name: 'Retry Notification' });
    fireEvent.click(retryBtn);

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith('/api/v1/admin/training/reports/controls/retry-notification/notif-99');
    });
  });
});
