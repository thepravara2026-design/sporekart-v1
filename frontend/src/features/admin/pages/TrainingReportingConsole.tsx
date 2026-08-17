import React, { useEffect, useState } from 'react';
import {
  ExecutiveOverviewDto,
  BatchUtilizationReportItemDto,
  AuditLogItemDto,
  OperationalExceptionItemDto,
  fetchExecutiveOverview,
  fetchBatchUtilizationReport,
  fetchAuditHistory,
  fetchOperationalExceptions,
  retryFailedNotificationControl,
  retryCertificateGenerationControl,
} from '../api/batchApi';

export const TrainingReportingConsole: React.FC = () => {
  const [overview, setOverview] = useState<ExecutiveOverviewDto | null>(null);
  const [batchReports, setBatchReports] = useState<BatchUtilizationReportItemDto[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogItemDto[]>([]);
  const [exceptions, setExceptions] = useState<OperationalExceptionItemDto[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [actionMessage, setActionMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const [actorFilter, setActorFilter] = useState<string>('');
  const [enrollmentIdFilter, setEnrollmentIdFilter] = useState<string>('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const [ovData, batchData, auditData, excData] = await Promise.all([
        fetchExecutiveOverview(),
        fetchBatchUtilizationReport(),
        fetchAuditHistory(),
        fetchOperationalExceptions(),
      ]);
      setOverview(ovData);
      setBatchReports(batchData || []);
      setAuditLogs(auditData || []);
      setExceptions(excData || []);
    } catch (err: any) {
      console.error('Failed to load reporting dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAuditFilter = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const logs = await fetchAuditHistory({ actor: actorFilter, enrollmentId: enrollmentIdFilter });
      setAuditLogs(logs || []);
    } catch (err: any) {
      setActionMessage({ type: 'error', text: 'Failed to filter audit logs.' });
    }
  };

  const handleRetryNotification = async (notificationId: string) => {
    try {
      await retryFailedNotificationControl(notificationId);
      setActionMessage({ type: 'success', text: `Notification ${notificationId} retried successfully.` });
      loadDashboardData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: 'Failed to retry notification.' });
    }
  };

  const handleRetryCertificate = async (enrollmentId: string) => {
    try {
      await retryCertificateGenerationControl(enrollmentId);
      setActionMessage({ type: 'success', text: `Certificate generation retried for enrollment ${enrollmentId}.` });
      loadDashboardData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: 'Failed to retry certificate generation.' });
    }
  };

  const downloadCsv = (endpoint: string, filename: string) => {
    const token = localStorage.getItem('token');
    fetch(endpoint, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => res.blob())
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
      })
      .catch(() => setActionMessage({ type: 'error', text: 'CSV export failed' }));
  };

  if (loading) {
    return <div className="p-8 text-center text-gray-400">Loading Operational Reporting Console...</div>;
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-8 bg-gray-900 text-gray-100 min-h-screen">
      {/* Header */}
      <div className="flex justify-between items-center border-b border-gray-800 pb-4">
        <div>
          <h1 className="text-2xl font-bold text-emerald-400">Training Operations & Audit Console</h1>
          <p className="text-sm text-gray-400">Real-time metrics, immutable audit explorer, consolidated exception queue & controls</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={() => downloadCsv('/api/v1/admin/training/reports/export/enrollments', 'enrollments_report.csv')}
            className="px-4 py-2 bg-emerald-700 hover:bg-emerald-600 rounded text-sm font-semibold transition"
          >
            Export Enrollments CSV
          </button>
          <button
            onClick={() => downloadCsv('/api/v1/admin/training/reports/export/audit', 'audit_history_report.csv')}
            className="px-4 py-2 bg-indigo-700 hover:bg-indigo-600 rounded text-sm font-semibold transition"
          >
            Export Audit CSV
          </button>
        </div>
      </div>

      {actionMessage && (
        <div className={`p-4 rounded text-sm font-medium ${actionMessage.type === 'success' ? 'bg-emerald-950 text-emerald-300 border border-emerald-800' : 'bg-red-950 text-red-300 border border-red-800'}`}>
          {actionMessage.text}
        </div>
      )}

      {/* KPI Overview Cards */}
      {overview && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-gray-800 p-5 rounded-lg border border-gray-700">
            <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Active Programs</span>
            <div className="text-2xl font-bold text-white mt-1">{overview.activeProgramsCount}</div>
            <div className="text-xs text-gray-400 mt-1">{overview.activeBatches} active / {overview.totalBatches} total batches</div>
          </div>
          <div className="bg-gray-800 p-5 rounded-lg border border-gray-700">
            <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Seat Utilization</span>
            <div className="text-2xl font-bold text-emerald-400 mt-1">{overview.seatUtilizationPercentage}%</div>
            <div className="text-xs text-gray-400 mt-1">{overview.totalOccupiedSeats} occupied / {overview.totalCapacitySeats} capacity ({overview.totalAvailableSeats} available)</div>
          </div>
          <div className="bg-gray-800 p-5 rounded-lg border border-gray-700">
            <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Net Training Revenue</span>
            <div className="text-2xl font-bold text-indigo-400 mt-1">${overview.netRevenue}</div>
            <div className="text-xs text-gray-400 mt-1">Gross: ${overview.grossRevenue} | Refunds: ${overview.totalRefunds}</div>
          </div>
          <div className="bg-gray-800 p-5 rounded-lg border border-gray-700">
            <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Certificates Issued</span>
            <div className="text-2xl font-bold text-amber-400 mt-1">{overview.certificatesIssuedCount}</div>
            <div className="text-xs text-gray-400 mt-1">{overview.completedEnrollments} completed enrollments</div>
          </div>
        </div>
      )}

      {/* Consolidated Exception Queue */}
      <div className="bg-gray-800 rounded-lg border border-gray-700 p-6">
        <h2 className="text-lg font-bold text-red-400 mb-4 flex items-center">
          <span className="mr-2">⚠️</span> Consolidated Operational Exception Queue ({exceptions.length})
        </h2>
        {exceptions.length === 0 ? (
          <div className="text-sm text-gray-400 py-4 text-center">No active operational exceptions or system anomalies detected.</div>
        ) : (
          <div className="divide-y divide-gray-700">
            {exceptions.map(exc => (
              <div key={exc.exceptionId} className="py-3 flex justify-between items-center">
                <div>
                  <div className="flex items-center space-x-2">
                    <span className={`px-2 py-0.5 text-xs rounded font-bold ${exc.severity === 'HIGH' ? 'bg-red-900 text-red-200' : 'bg-amber-900 text-amber-200'}`}>
                      {exc.severity}
                    </span>
                    <span className="text-sm font-semibold text-white">{exc.type}</span>
                  </div>
                  <p className="text-xs text-gray-400 mt-1">{exc.description}</p>
                </div>
                <div>
                  {exc.type === 'FAILED_NOTIFICATION' && (
                    <button
                      onClick={() => handleRetryNotification(exc.resourceId)}
                      className="px-3 py-1 bg-amber-600 hover:bg-amber-500 text-xs rounded font-semibold text-white transition"
                    >
                      Retry Notification
                    </button>
                  )}
                  {exc.type === 'FAILED_CERTIFICATE' && (
                    <button
                      onClick={() => handleRetryCertificate(exc.resourceId)}
                      className="px-3 py-1 bg-amber-600 hover:bg-amber-500 text-xs rounded font-semibold text-white transition"
                    >
                      Retry Certificate
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Batch Utilization Table */}
      <div className="bg-gray-800 rounded-lg border border-gray-700 p-6">
        <h2 className="text-lg font-bold text-emerald-400 mb-4">Batch Capacity & Utilization Breakdown</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-300">
            <thead className="bg-gray-900 text-gray-400 uppercase text-xs">
              <tr>
                <th className="p-3">Batch Code</th>
                <th className="p-3">Status</th>
                <th className="p-3">Occupied / Total</th>
                <th className="p-3">Available</th>
                <th className="p-3">Utilization</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {batchReports.map(b => (
                <tr key={b.batchId} className="hover:bg-gray-750">
                  <td className="p-3 font-mono font-medium text-white">{b.batchCode}</td>
                  <td className="p-3">
                    <span className={`px-2 py-0.5 text-xs rounded font-semibold ${b.status === 'ACTIVE' ? 'bg-emerald-900 text-emerald-200' : 'bg-gray-700 text-gray-300'}`}>
                      {b.status}
                    </span>
                  </td>
                  <td className="p-3">{b.occupiedSeats} / {b.totalSeats}</td>
                  <td className="p-3 text-emerald-400 font-semibold">{b.availableSeats}</td>
                  <td className="p-3">
                    <div className="flex items-center space-x-3">
                      <div className="w-24 bg-gray-700 h-2 rounded-full overflow-hidden">
                        <div
                          className={`h-full ${b.utilizationPercentage >= 90 ? 'bg-red-500' : b.utilizationPercentage >= 75 ? 'bg-amber-400' : 'bg-emerald-500'}`}
                          style={{ width: `${Math.min(100, b.utilizationPercentage)}%` }}
                        />
                      </div>
                      <span className="text-xs font-mono">{b.utilizationPercentage}%</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Immutable Audit Log Explorer */}
      <div className="bg-gray-800 rounded-lg border border-gray-700 p-6 space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-lg font-bold text-indigo-400">Immutable Enrollment Audit Log Explorer</h2>
          <form onSubmit={handleAuditFilter} className="flex space-x-2">
            <input
              type="text"
              placeholder="Filter by Actor email"
              value={actorFilter}
              onChange={e => setActorFilter(e.target.value)}
              className="px-3 py-1 bg-gray-900 border border-gray-700 rounded text-xs text-white"
            />
            <input
              type="text"
              placeholder="Filter by Enrollment ID"
              value={enrollmentIdFilter}
              onChange={e => setEnrollmentIdFilter(e.target.value)}
              className="px-3 py-1 bg-gray-900 border border-gray-700 rounded text-xs text-white"
            />
            <button type="submit" className="px-3 py-1 bg-indigo-600 hover:bg-indigo-500 text-xs rounded text-white font-semibold">
              Search
            </button>
          </form>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-300">
            <thead className="bg-gray-900 text-gray-400 uppercase text-xs">
              <tr>
                <th className="p-3">Timestamp</th>
                <th className="p-3">Enrollment ID</th>
                <th className="p-3">Transition</th>
                <th className="p-3">Reason</th>
                <th className="p-3">Actor</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {auditLogs.slice(0, 20).map(logItem => (
                <tr key={logItem.id} className="hover:bg-gray-750 font-mono text-xs">
                  <td className="p-3 text-gray-400">{logItem.timestamp}</td>
                  <td className="p-3 text-white">{logItem.enrollmentId}</td>
                  <td className="p-3">
                    <span className="text-gray-400">{logItem.fromStatus}</span>
                    <span className="mx-2 text-indigo-400">➔</span>
                    <span className="text-emerald-400 font-bold">{logItem.toStatus}</span>
                  </td>
                  <td className="p-3 text-gray-300">{logItem.reason || '-'}</td>
                  <td className="p-3 text-indigo-300">{logItem.actor}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
