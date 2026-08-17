import React, { useEffect, useState } from 'react';
import { axiosInstance } from '../../../services/apiClient';

export interface ProviderHealth {
  name: string;
  channel: string;
  mode?: string;
  status: 'HEALTHY' | 'DEGRADED' | 'UNAVAILABLE' | 'DISABLED' | 'UNKNOWN';
  circuitState: 'CLOSED' | 'OPEN' | 'HALF_OPEN';
  consecutiveFailures: number;
  failureCategory?: string;
  averageLatencyMs?: number;
}

export interface BacklogSummary {
  createdCount: number;
  queuedCount: number;
  processingCount: number;
  retryScheduledCount: number;
  sentCount: number;
  deliveredCount: number;
  failedCount: number;
  failedPermanentlyCount: number;
  cancelledCount: number;
  suppressedCount: number;
  staleProcessingCount: number;
  retryBacklogCount: number;
  reconciliationBacklogCount: number;
  oldestPendingAgeSeconds: number;
  oldestRetryAgeSeconds: number;
  oldestReconciliationAgeSeconds: number;
}

export interface AlertCondition {
  alertType: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  providerName: string;
  channel: string;
  message: string;
  timestamp: string;
}

export interface NotificationDetail {
  id: string;
  eventId?: string;
  eventType: string;
  userId?: string;
  customerId?: string;
  channel: string;
  templateCode: string;
  recipient: string;
  subject?: string;
  status: string;
  priority: string;
  providerName?: string;
  providerMessageId?: string;
  providerStatus?: string;
  circuitState?: string;
  attemptCount: number;
  createdAt: string;
  updatedAt: string;
  deliveredAt?: string;
  failedAt?: string;
  failureReason?: string;
  lastAttemptAt?: string;
  nextRetryAt?: string;
  reconciliationAttemptCount?: number;
  lastReconciliationAt?: string;
}

export interface TimelineEvent {
  eventType: string;
  timestamp: string;
  providerName?: string;
  providerMessageId?: string;
  details?: string;
  description: string;
}

export const NotificationOperationsConsole: React.FC = () => {
  const [overallStatus, setOverallStatus] = useState<string>('HEALTHY');
  const [providers, setProviders] = useState<ProviderHealth[]>([]);
  const [backlog, setBacklog] = useState<BacklogSummary | null>(null);
  const [alerts, setAlerts] = useState<AlertCondition[]>([]);
  const [notifications, setNotifications] = useState<NotificationDetail[]>([]);
  const [selectedNotification, setSelectedNotification] = useState<NotificationDetail | null>(null);
  const [timeline, setTimeline] = useState<TimelineEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [channelFilter, setChannelFilter] = useState<string>('');
  const [providerFilter, setProviderFilter] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);

  const fetchHealthAndBacklog = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await axiosInstance.get('/api/v1/admin/notifications/health');
      if (res.data && res.data.data) {
        const data = res.data.data;
        setOverallStatus(data.overallStatus || 'HEALTHY');
        setAlerts(data.activeAlerts || []);
        setBacklog(data.backlog || null);
        if (data.resilience && data.resilience.providers) {
          setProviders(data.resilience.providers);
        }
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load notification operations health';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const fetchNotifications = async () => {
    try {
      const params: Record<string, string | number> = { page, size: 10 };
      if (statusFilter) params.status = statusFilter;
      if (channelFilter) params.channel = channelFilter;
      if (providerFilter) params.provider = providerFilter;

      const res = await axiosInstance.get('/api/v1/admin/notifications', { params });
      if (res.data && res.data.data) {
        setNotifications(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch {
      // Keep silent for list updates
    }
  };

  useEffect(() => {
    fetchHealthAndBacklog();
  }, []);

  useEffect(() => {
    fetchNotifications();
  }, [page, statusFilter, channelFilter, providerFilter]);

  const handleSelectNotification = async (notif: NotificationDetail) => {
    setSelectedNotification(notif);
    try {
      const res = await axiosInstance.get(`/api/v1/admin/notifications/${notif.id}/timeline`);
      if (res.data && res.data.data) {
        setTimeline(res.data.data || []);
      }
    } catch {
      setTimeline([]);
    }
  };

  const handleRetry = async (id: string) => {
    try {
      await axiosInstance.post(`/api/v1/admin/notifications/${id}/retry`);
      fetchHealthAndBacklog();
      fetchNotifications();
      if (selectedNotification && selectedNotification.id === id) {
        handleSelectNotification(selectedNotification);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error executing retry';
      alert('Retry failed: ' + msg);
    }
  };

  const handleCancel = async (id: string) => {
    try {
      await axiosInstance.post(`/api/v1/admin/notifications/${id}/cancel`, { reason: 'Cancelled via Admin Console' });
      fetchHealthAndBacklog();
      fetchNotifications();
      setSelectedNotification(null);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error executing cancel';
      alert('Cancel failed: ' + msg);
    }
  };

  const handleStaleRecover = async () => {
    try {
      const res = await axiosInstance.post('/api/v1/admin/notifications/stale-recover');
      alert(`Stale recovery completed. Recovered: ${res.data?.data?.recoveredCount || 0}`);
      fetchHealthAndBacklog();
      fetchNotifications();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error executing recovery';
      alert('Stale recovery failed: ' + msg);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DELIVERED':
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-emerald-100 text-emerald-800">DELIVERED</span>;
      case 'SENT':
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-blue-100 text-blue-800">SENT</span>;
      case 'PROCESSING':
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-amber-100 text-amber-800 animate-pulse">PROCESSING</span>;
      case 'RETRY_SCHEDULED':
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-orange-100 text-orange-800">RETRY_SCHEDULED</span>;
      case 'FAILED_PERMANENTLY':
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-rose-100 text-rose-800">FAILED_PERMANENTLY</span>;
      case 'CANCELLED':
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-gray-100 text-gray-800">CANCELLED</span>;
      case 'SUPPRESSED':
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-purple-100 text-purple-800">SUPPRESSED</span>;
      default:
        return <span className="px-2 py-1 text-xs font-semibold rounded bg-gray-100 text-gray-600">{status}</span>;
    }
  };

  const getHealthBadge = (status: string) => {
    if (status === 'HEALTHY') return <span className="px-3 py-1 font-bold text-sm rounded-full bg-emerald-500 text-white">HEALTHY</span>;
    if (status === 'DEGRADED') return <span className="px-3 py-1 font-bold text-sm rounded-full bg-amber-500 text-white">DEGRADED</span>;
    return <span className="px-3 py-1 font-bold text-sm rounded-full bg-rose-600 text-white">CRITICAL</span>;
  };

  const getCircuitBadge = (state: string) => {
    if (state === 'CLOSED') return <span className="px-2 py-0.5 text-xs font-bold rounded bg-emerald-100 text-emerald-700">CLOSED</span>;
    if (state === 'HALF_OPEN') return <span className="px-2 py-0.5 text-xs font-bold rounded bg-amber-100 text-amber-700">HALF_OPEN</span>;
    return <span className="px-2 py-0.5 text-xs font-bold rounded bg-rose-100 text-rose-700">OPEN</span>;
  };

  if (loading) {
    return (
      <div className="p-8 text-center text-gray-600 font-medium">
        Loading Notification Operations Console...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 p-6 space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center bg-slate-800 border border-slate-700 p-6 rounded-xl shadow-lg">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Notification Operations Console</h1>
          <p className="text-sm text-slate-400 mt-1">Real-time observability, provider resilience, and delivery administration</p>
        </div>
        <div className="mt-4 md:mt-0 flex items-center space-x-4">
          <button
            onClick={handleStaleRecover}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-lg shadow transition"
          >
            Trigger Stale Recovery
          </button>
          <button
            onClick={fetchHealthAndBacklog}
            className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-200 text-xs font-semibold rounded-lg shadow transition"
          >
            Refresh Data
          </button>
          <div className="flex items-center space-x-2">
            <span className="text-xs text-slate-400 font-medium">System Health:</span>
            {getHealthBadge(overallStatus)}
          </div>
        </div>
      </div>

      {error && (
        <div className="bg-rose-900/50 border border-rose-700 p-4 rounded-lg text-rose-200 text-sm">
          {error}
        </div>
      )}

      {/* Active Alerts */}
      {alerts.length > 0 && (
        <div className="bg-slate-800 border border-amber-500/30 p-4 rounded-xl space-y-2">
          <h3 className="text-sm font-semibold text-amber-400 uppercase tracking-wider flex items-center gap-2">
            ⚠️ Active Operational Alerts ({alerts.length})
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {alerts.map((al, idx) => (
              <div key={idx} className="bg-slate-900/80 p-3 rounded-lg border border-slate-700 text-xs flex justify-between items-start">
                <div>
                  <span className={`font-bold mr-2 ${al.severity === 'CRITICAL' ? 'text-rose-400' : 'text-amber-400'}`}>
                    [{al.severity}] {al.alertType}
                  </span>
                  <p className="text-slate-300 mt-1">{al.message}</p>
                </div>
                <span className="text-slate-500 text-[10px]">{new Date(al.timestamp).toLocaleTimeString()}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Backlog Metrics Grid */}
      {backlog && (
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-4">
          <div className="bg-slate-800 border border-slate-700 p-4 rounded-xl">
            <span className="text-xs text-slate-400 block font-medium">Processing</span>
            <span className="text-2xl font-extrabold text-amber-400 mt-1 block">{backlog.processingCount}</span>
            <span className="text-[10px] text-slate-500">Stale: {backlog.staleProcessingCount}</span>
          </div>

          <div className="bg-slate-800 border border-slate-700 p-4 rounded-xl">
            <span className="text-xs text-slate-400 block font-medium">Retry Backlog</span>
            <span className="text-2xl font-extrabold text-orange-400 mt-1 block">{backlog.retryBacklogCount}</span>
            <span className="text-[10px] text-slate-500">Oldest: {backlog.oldestRetryAgeSeconds}s</span>
          </div>

          <div className="bg-slate-800 border border-slate-700 p-4 rounded-xl">
            <span className="text-xs text-slate-400 block font-medium">Reconcile Queue</span>
            <span className="text-2xl font-extrabold text-sky-400 mt-1 block">{backlog.reconciliationBacklogCount}</span>
            <span className="text-[10px] text-slate-500">Oldest: {backlog.oldestReconciliationAgeSeconds}s</span>
          </div>

          <div className="bg-slate-800 border border-slate-700 p-4 rounded-xl">
            <span className="text-xs text-slate-400 block font-medium">Delivered</span>
            <span className="text-2xl font-extrabold text-emerald-400 mt-1 block">{backlog.deliveredCount}</span>
            <span className="text-[10px] text-slate-500">Sent: {backlog.sentCount}</span>
          </div>

          <div className="bg-slate-800 border border-slate-700 p-4 rounded-xl">
            <span className="text-xs text-slate-400 block font-medium">Failed Permanent</span>
            <span className="text-2xl font-extrabold text-rose-400 mt-1 block">{backlog.failedPermanentlyCount}</span>
            <span className="text-[10px] text-slate-500">Non-retryable</span>
          </div>

          <div className="bg-slate-800 border border-slate-700 p-4 rounded-xl">
            <span className="text-xs text-slate-400 block font-medium">Suppressed/Cancel</span>
            <span className="text-2xl font-extrabold text-purple-400 mt-1 block">{backlog.suppressedCount + backlog.cancelledCount}</span>
            <span className="text-[10px] text-slate-500">Policy enforced</span>
          </div>
        </div>
      )}

      {/* Provider Health Matrix */}
      <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 shadow-lg">
        <h2 className="text-lg font-bold text-white mb-4">Provider Resilience Matrix</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-900/60 uppercase text-[10px] text-slate-400 tracking-wider">
              <tr>
                <th className="p-3">Provider</th>
                <th className="p-3">Channel</th>
                <th className="p-3">Mode</th>
                <th className="p-3">Health</th>
                <th className="p-3">Circuit State</th>
                <th className="p-3">Consecutive Failures</th>
                <th className="p-3">Failure Category</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/50">
              {providers.map((pr, idx) => (
                <tr key={idx} className="hover:bg-slate-700/30 transition">
                  <td className="p-3 font-semibold text-white">{pr.name}</td>
                  <td className="p-3 font-mono text-slate-400">{pr.channel}</td>
                  <td className="p-3 font-mono text-slate-400">{pr.mode || 'MOCK/REAL'}</td>
                  <td className="p-3">{getHealthBadge(pr.status)}</td>
                  <td className="p-3">{getCircuitBadge(pr.circuitState)}</td>
                  <td className="p-3 font-mono">{pr.consecutiveFailures}</td>
                  <td className="p-3 font-mono text-amber-400">{pr.failureCategory || 'NONE'}</td>
                </tr>
              ))}
              {providers.length === 0 && (
                <tr>
                  <td colSpan={7} className="p-4 text-center text-slate-500">No provider metrics available</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Notifications Inspection & Filters */}
      <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 shadow-lg space-y-4">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <h2 className="text-lg font-bold text-white">Notification Operations Stream</h2>

          {/* Filters */}
          <div className="flex flex-wrap gap-2 text-xs">
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              className="bg-slate-900 border border-slate-700 rounded px-2 py-1 text-slate-200"
            >
              <option value="">All Statuses</option>
              <option value="PROCESSING">PROCESSING</option>
              <option value="RETRY_SCHEDULED">RETRY_SCHEDULED</option>
              <option value="SENT">SENT</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="FAILED_PERMANENTLY">FAILED_PERMANENTLY</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>

            <select
              value={channelFilter}
              onChange={(e) => { setChannelFilter(e.target.value); setPage(0); }}
              className="bg-slate-900 border border-slate-700 rounded px-2 py-1 text-slate-200"
            >
              <option value="">All Channels</option>
              <option value="EMAIL">EMAIL</option>
              <option value="SMS">SMS</option>
              <option value="WHATSAPP">WHATSAPP</option>
              <option value="PUSH">PUSH</option>
              <option value="IN_APP">IN_APP</option>
            </select>

            <select
              value={providerFilter}
              onChange={(e) => { setProviderFilter(e.target.value); setPage(0); }}
              className="bg-slate-900 border border-slate-700 rounded px-2 py-1 text-slate-200"
            >
              <option value="">All Providers</option>
              <option value="SendGrid">SendGrid</option>
              <option value="Twilio">Twilio</option>
              <option value="MetaWhatsApp">MetaWhatsApp</option>
              <option value="MockEmailProvider">MockEmailProvider</option>
              <option value="MockSmsProvider">MockSmsProvider</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-900/60 uppercase text-[10px] text-slate-400 tracking-wider">
              <tr>
                <th className="p-3">Notification ID</th>
                <th className="p-3">Channel</th>
                <th className="p-3">Template</th>
                <th className="p-3">Recipient</th>
                <th className="p-3">Status</th>
                <th className="p-3">Provider</th>
                <th className="p-3">Attempts</th>
                <th className="p-3">Created At</th>
                <th className="p-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/50">
              {notifications.map((notif) => (
                <tr key={notif.id} className="hover:bg-slate-700/30 transition">
                  <td className="p-3 font-mono text-indigo-400 hover:underline cursor-pointer" onClick={() => handleSelectNotification(notif)}>
                    {notif.id.substring(0, 8)}...
                  </td>
                  <td className="p-3 font-mono">{notif.channel}</td>
                  <td className="p-3 font-mono">{notif.templateCode}</td>
                  <td className="p-3 font-mono">{notif.recipient}</td>
                  <td className="p-3">{getStatusBadge(notif.status)}</td>
                  <td className="p-3 font-mono">{notif.providerName || '-'}</td>
                  <td className="p-3 font-mono">{notif.attemptCount}</td>
                  <td className="p-3 text-slate-400">{new Date(notif.createdAt).toLocaleTimeString()}</td>
                  <td className="p-3 space-x-2">
                    {['RETRY_SCHEDULED', 'FAILED', 'FAILED_PERMANENTLY'].includes(notif.status) && (
                      <button
                        onClick={() => handleRetry(notif.id)}
                        className="px-2 py-0.5 bg-indigo-600 hover:bg-indigo-500 text-white text-[10px] font-bold rounded"
                      >
                        Retry
                      </button>
                    )}
                    {!['DELIVERED', 'FAILED_PERMANENTLY', 'CANCELLED', 'SUPPRESSED'].includes(notif.status) && (
                      <button
                        onClick={() => handleCancel(notif.id)}
                        className="px-2 py-0.5 bg-rose-700 hover:bg-rose-600 text-white text-[10px] font-bold rounded"
                      >
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {notifications.length === 0 && (
                <tr>
                  <td colSpan={9} className="p-4 text-center text-slate-500">No notifications found matching filter criteria</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="flex justify-between items-center text-xs text-slate-400 pt-2">
          <span>Page {page + 1} of {totalPages}</span>
          <div className="space-x-2">
            <button
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
              className="px-3 py-1 bg-slate-700 hover:bg-slate-600 disabled:opacity-50 text-white rounded font-medium"
            >
              Previous
            </button>
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => p + 1)}
              className="px-3 py-1 bg-slate-700 hover:bg-slate-600 disabled:opacity-50 text-white rounded font-medium"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Notification Detail & Delivery Timeline Modal */}
      {selectedNotification && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center p-4 z-50">
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 max-w-2xl w-full space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-start border-b border-slate-700 pb-3">
              <div>
                <h3 className="text-lg font-bold text-white">Notification Details & Timeline</h3>
                <p className="text-xs font-mono text-slate-400">{selectedNotification.id}</p>
              </div>
              <button
                onClick={() => setSelectedNotification(null)}
                className="text-slate-400 hover:text-white font-bold text-lg"
              >
                ✕
              </button>
            </div>

            <div className="grid grid-cols-2 gap-4 text-xs font-mono">
              <div><span className="text-slate-500">Status:</span> {getStatusBadge(selectedNotification.status)}</div>
              <div><span className="text-slate-500">Channel:</span> {selectedNotification.channel}</div>
              <div><span className="text-slate-500">Template:</span> {selectedNotification.templateCode}</div>
              <div><span className="text-slate-500">Recipient:</span> {selectedNotification.recipient}</div>
              <div><span className="text-slate-500">Provider:</span> {selectedNotification.providerName || 'N/A'}</div>
              <div><span className="text-slate-500">Provider Msg ID:</span> {selectedNotification.providerMessageId || 'N/A'}</div>
            </div>

            {selectedNotification.failureReason && (
              <div className="bg-rose-900/40 border border-rose-800/60 p-3 rounded text-xs text-rose-300 font-mono">
                <span className="font-bold">Failure Reason:</span> {selectedNotification.failureReason}
              </div>
            )}

            {/* Delivery Timeline */}
            <div className="space-y-3 pt-2">
              <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider">Delivery Timeline</h4>
              <div className="space-y-2 border-l-2 border-slate-700 pl-4">
                {timeline.map((evt, idx) => (
                  <div key={idx} className="relative text-xs space-y-0.5">
                    <div className="absolute -left-[21px] top-1 w-2.5 h-2.5 rounded-full bg-indigo-500 border border-slate-800"></div>
                    <div className="flex justify-between text-slate-300 font-semibold">
                      <span>{evt.eventType}</span>
                      <span className="text-[10px] text-slate-500 font-mono">{new Date(evt.timestamp).toLocaleString()}</span>
                    </div>
                    <p className="text-slate-400 text-[11px]">{evt.description}</p>
                    {evt.details && <p className="text-rose-400 text-[10px] font-mono">{evt.details}</p>}
                  </div>
                ))}
              </div>
            </div>

            <div className="flex justify-end space-x-2 border-t border-slate-700 pt-3">
              <button
                onClick={() => setSelectedNotification(null)}
                className="px-4 py-1.5 bg-slate-700 hover:bg-slate-600 text-white text-xs font-semibold rounded"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
