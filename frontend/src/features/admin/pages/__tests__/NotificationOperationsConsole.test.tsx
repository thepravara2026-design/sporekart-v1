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

  const mockRetention = {
    status: 'HEALTHY',
    enabled: true,
    dryRun: false,
    notificationRetentionDays: 30,
    payloadRetentionDays: 7,
    auditRetentionDays: 90,
    outboxRetentionDays: 14,
    eligibleNotificationsForDeletion: 5,
    eligibleNotificationsForPayloadMinimization: 2,
    eligibleOutboxEventsForDeletion: 10,
    totalRecordsDeleted: 100,
    totalPayloadsMinimized: 20,
    totalOutboxCleaned: 50,
    totalFailures: 0,
  };

  const mockIntelligence = {
    timeWindow: '24h',
    totalNotifications: 50,
    deliveredNotifications: 45,
    failedNotifications: 2,
    cancelledNotifications: 1,
    suppressedNotifications: 2,
    retriedNotifications: 3,
    successRatePercent: 90.0,
    failureRatePercent: 4.0,
    cancellationRatePercent: 2.0,
    suppressionRatePercent: 4.0,
    retryRatePercent: 6.0,
    providerAggregates: [
      {
        providerName: 'SendGrid',
        channel: 'EMAIL',
        sentCount: 50,
        deliveredCount: 45,
        failedCount: 2,
        successRatePercent: 90.0,
        averageDeliveryLatencyMs: 250.0,
      },
    ],
    backlogAging: {
      under1mCount: 2,
      between1mAnd5mCount: 0,
      between5mAnd15mCount: 0,
      over15mCount: 0,
    },
  };

  it('renders health status, providers matrix, and backlog metrics cleanly when query succeeds', async () => {
    vi.mocked(axiosInstance.get).mockImplementation((url: string) => {
      if (url.includes('/retention')) {
        return Promise.resolve({ data: { data: mockRetention } });
      }
      if (url.includes('/intelligence')) {
        return Promise.resolve({ data: { data: mockIntelligence } });
      }
      if (url.includes('/health')) {
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
      if (url.includes('/notifications')) {
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
      return Promise.resolve({ data: { data: {} } });
    });

    render(<NotificationOperationsConsole />);

    await waitFor(() => {
      expect(screen.getByText('Notification Operations Console')).toBeInTheDocument();
    });

    expect(screen.getAllByText('HEALTHY')[0]).toBeInTheDocument();
    expect(screen.getAllByText('SendGrid')[0]).toBeInTheDocument();
  });

  it('renders active operational alerts when system is degraded or critical', async () => {
    vi.mocked(axiosInstance.get).mockImplementation((url: string) => {
      if (url.includes('/retention')) {
        return Promise.resolve({ data: { data: mockRetention } });
      }
      if (url.includes('/intelligence')) {
        return Promise.resolve({ data: { data: mockIntelligence } });
      }
      if (url.includes('/health')) {
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
      if (url.includes('/notifications')) {
        return Promise.resolve({
          data: {
            data: {
              content: [],
              totalPages: 1,
            },
          },
        });
      }
      return Promise.resolve({ data: { data: {} } });
    });

    render(<NotificationOperationsConsole />);

    await waitFor(() => {
      expect(screen.getAllByText('DEGRADED')[0]).toBeInTheDocument();
      expect(screen.getByText(/Circuit breaker is OPEN for provider Twilio/i)).toBeInTheDocument();
    });
  });
});
