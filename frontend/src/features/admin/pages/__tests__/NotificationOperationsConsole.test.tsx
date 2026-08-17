import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NotificationOperationsConsole } from '../NotificationOperationsConsole';
import { axiosInstance } from '../../../../services/apiClient';

vi.mock('../../../../services/apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('NotificationOperationsConsole Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders health status, providers matrix, and backlog metrics cleanly when query succeeds', async () => {
    vi.mocked(axiosInstance.get).mockImplementation((url: string) => {
      if (url === '/api/v1/admin/notifications/health') {
        return Promise.resolve({
          data: {
            data: {
              overallStatus: 'HEALTHY',
              activeAlerts: [],
              backlog: {
                createdCount: 0,
                queuedCount: 0,
                processingCount: 2,
                retryScheduledCount: 1,
                sentCount: 10,
                deliveredCount: 8,
                failedCount: 0,
                failedPermanentlyCount: 1,
                cancelledCount: 0,
                suppressedCount: 0,
                staleProcessingCount: 0,
                retryBacklogCount: 1,
                reconciliationBacklogCount: 0,
                oldestPendingAgeSeconds: 0,
                oldestRetryAgeSeconds: 5,
                oldestReconciliationAgeSeconds: 0,
              },
              resilience: {
                providers: [
                  {
                    name: 'SendGrid',
                    channel: 'EMAIL',
                    mode: 'REAL',
                    status: 'HEALTHY',
                    circuitState: 'CLOSED',
                    consecutiveFailures: 0,
                    failureCategory: 'NONE',
                  },
                ],
              },
            },
          },
        });
      }
      if (url === '/api/v1/admin/notifications') {
        return Promise.resolve({
          data: {
            data: {
              content: [
                {
                  id: 'notif-7i-001',
                  channel: 'EMAIL',
                  templateCode: 'ORDER_CONFIRMATION',
                  recipient: 'us***@example.com',
                  status: 'DELIVERED',
                  attemptCount: 1,
                  createdAt: new Date().toISOString(),
                  updatedAt: new Date().toISOString(),
                },
              ],
              totalPages: 1,
            },
          },
        });
      }
      return Promise.reject(new Error('Unknown endpoint'));
    });

    render(<NotificationOperationsConsole />);

    await waitFor(() => {
      expect(screen.getByText('Notification Operations Console')).toBeInTheDocument();
      expect(screen.getAllByText('HEALTHY')[0]).toBeInTheDocument();
      expect(screen.getByText('SendGrid')).toBeInTheDocument();
      expect(screen.getByText('ORDER_CONFIRMATION')).toBeInTheDocument();
    });
  });

  it('renders active operational alerts when system is degraded or critical', async () => {
    vi.mocked(axiosInstance.get).mockImplementation((url: string) => {
      if (url === '/api/v1/admin/notifications/health') {
        return Promise.resolve({
          data: {
            data: {
              overallStatus: 'DEGRADED',
              activeAlerts: [
                {
                  alertType: 'PROVIDER_CIRCUIT_OPEN',
                  severity: 'HIGH',
                  providerName: 'Twilio',
                  channel: 'SMS',
                  message: 'Circuit breaker is OPEN for provider Twilio (SMS)',
                  timestamp: new Date().toISOString(),
                },
              ],
              backlog: {
                createdCount: 0,
                queuedCount: 0,
                processingCount: 0,
                retryScheduledCount: 5,
                sentCount: 2,
                deliveredCount: 2,
                failedCount: 0,
                failedPermanentlyCount: 0,
                suppressedCount: 0,
                cancelledCount: 0,
                staleProcessingCount: 0,
                retryBacklogCount: 5,
                reconciliationBacklogCount: 0,
                oldestPendingAgeSeconds: 0,
                oldestRetryAgeSeconds: 120,
                oldestReconciliationAgeSeconds: 0,
              },
              resilience: {
                providers: [
                  {
                    name: 'Twilio',
                    channel: 'SMS',
                    mode: 'REAL',
                    status: 'DEGRADED',
                    circuitState: 'OPEN',
                    consecutiveFailures: 3,
                    failureCategory: 'TIMEOUT',
                    lastFailureCategory: 'TIMEOUT',
                  },
                ],
              },
            },
          },
        });
      }
      if (url === '/api/v1/admin/notifications') {
        return Promise.resolve({
          data: {
            data: {
              content: [],
              totalPages: 1,
            },
          },
        });
      }
      return Promise.reject(new Error('Unknown endpoint'));
    });

    render(<NotificationOperationsConsole />);

    await waitFor(() => {
      expect(screen.getAllByText('DEGRADED')[0]).toBeInTheDocument();
      expect(screen.getByText(/Circuit breaker is OPEN for provider Twilio/i)).toBeInTheDocument();
    });
  });
});
