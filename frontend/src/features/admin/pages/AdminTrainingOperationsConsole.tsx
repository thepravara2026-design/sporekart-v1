import React, { useEffect, useState } from 'react';
import {
  AdminTrainingDashboardDto,
  EnrollmentLifecycleDto,
  DemandDto,
  TrainingPaymentStatusDto,
  fetchAdminDashboard,
  fetchAdminGlobalEnrollments,
  fetchAdminPaymentExceptions,
  fetchAdminGlobalDemands,
  triggerEnrollmentRecovery,
  fetchAdminEnrollmentDetails,
  fetchMyEnrollmentHistory,
  EnrollmentHistoryDto
} from '../api/batchApi';
import { TrainingProgramManagement } from './TrainingProgramManagement';
import { BatchManagementConsole } from './BatchManagementConsole';

export const AdminTrainingOperationsConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'programs' | 'batches' | 'enrollments' | 'demand'>('overview');

  // Dashboard metrics state
  const [dashboard, setDashboard] = useState<AdminTrainingDashboardDto | null>(null);
  const [dashboardLoading, setDashboardLoading] = useState<boolean>(true);
  const [dashboardError, setDashboardError] = useState<string | null>(null);

  // Exceptions state
  const [exceptions, setExceptions] = useState<TrainingPaymentStatusDto[]>([]);
  const [exceptionsLoading, setExceptionsLoading] = useState<boolean>(false);

  // Global Enrollments state
  const [enrollments, setEnrollments] = useState<EnrollmentLifecycleDto[]>([]);
  const [enrollmentsLoading, setEnrollmentsLoading] = useState<boolean>(false);
  const [enrollmentStatusFilter, setEnrollmentStatusFilter] = useState<string>('');
  const [enrollmentSearch, setEnrollmentSearch] = useState<string>('');
  const [enrollmentPage, setEnrollmentPage] = useState<number>(0);
  const [enrollmentTotalPages, setEnrollmentTotalPages] = useState<number>(1);
  const [selectedEnrollment, setSelectedEnrollment] = useState<EnrollmentLifecycleDto | null>(null);
  const [enrollmentHistory, setEnrollmentHistory] = useState<EnrollmentHistoryDto[]>([]);
  const [historyLoading, setHistoryLoading] = useState<boolean>(false);

  // Global Demand state
  const [demands, setDemands] = useState<DemandDto[]>([]);
  const [demandsLoading, setDemandsLoading] = useState<boolean>(false);
  const [demandStatusFilter, setDemandStatusFilter] = useState<string>('');
  const [demandSearch, setDemandSearch] = useState<string>('');
  const [demandPage, setDemandPage] = useState<number>(0);
  const [demandTotalPages, setDemandTotalPages] = useState<number>(1);

  // Action message state
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadDashboard = async () => {
    setDashboardLoading(true);
    setDashboardError(null);
    try {
      const data = await fetchAdminDashboard();
      setDashboard(data);
    } catch (err: any) {
      setDashboardError(err?.response?.data?.message || 'Failed to load training operations dashboard');
    } finally {
      setDashboardLoading(false);
    }
  };

  const loadExceptions = async () => {
    setExceptionsLoading(true);
    try {
      const data = await fetchAdminPaymentExceptions();
      setExceptions(data.content || []);
    } catch (err) {
      // Ignore exceptions fetch error quietly
    } finally {
      setExceptionsLoading(false);
    }
  };

  const loadEnrollments = async () => {
    setEnrollmentsLoading(true);
    try {
      const data = await fetchAdminGlobalEnrollments({
        status: enrollmentStatusFilter || undefined,
        search: enrollmentSearch || undefined,
        page: enrollmentPage,
        size: 10
      });
      setEnrollments(data.content || []);
      setEnrollmentTotalPages(data.totalPages || 1);
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to search enrollments');
    } finally {
      setEnrollmentsLoading(false);
    }
  };

  const loadDemands = async () => {
    setDemandsLoading(true);
    try {
      const data = await fetchAdminGlobalDemands({
        status: demandStatusFilter || undefined,
        search: demandSearch || undefined,
        page: demandPage,
        size: 10
      });
      setDemands(data.content || []);
      setDemandTotalPages(data.totalPages || 1);
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to search demand records');
    } finally {
      setDemandsLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
    loadExceptions();
  }, []);

  useEffect(() => {
    if (activeTab === 'enrollments') {
      loadEnrollments();
    }
  }, [activeTab, enrollmentStatusFilter, enrollmentSearch, enrollmentPage]);

  useEffect(() => {
    if (activeTab === 'demand') {
      loadDemands();
    }
  }, [activeTab, demandStatusFilter, demandSearch, demandPage]);

  const handleTriggerRecovery = async (enrollmentId: string) => {
    setActionSuccess(null);
    setActionError(null);
    try {
      const recovered = await triggerEnrollmentRecovery(enrollmentId);
      setActionSuccess(`Successfully recovered confirmation for enrollment ${recovered.enrollmentCode || recovered.id}!`);
      loadDashboard();
      loadExceptions();
      if (activeTab === 'enrollments') loadEnrollments();
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to trigger enrollment recovery');
    }
  };

  const handleViewEnrollmentDetails = async (enrollmentId: string) => {
    setHistoryLoading(true);
    try {
      const details = await fetchAdminEnrollmentDetails(enrollmentId);
      setSelectedEnrollment(details);
      const hist = await fetchMyEnrollmentHistory(enrollmentId);
      setEnrollmentHistory(hist);
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to fetch enrollment details');
    } finally {
      setHistoryLoading(false);
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'sans-serif' }}>
      <header style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', color: '#1e293b', marginBottom: '8px' }}>
          Admin Training Operations
        </h1>
        <p style={{ color: '#64748b' }}>
          Authoritative administration for training programs, batches, capacity, enrollments, demand, and payment exceptions.
        </p>
      </header>

      {/* Global Banner Messages */}
      {actionSuccess && (
        <div style={{ padding: '12px 16px', backgroundColor: '#dcfce7', color: '#166534', border: '1px solid #bbf7d0', borderRadius: '6px', marginBottom: '16px' }}>
          {actionSuccess}
        </div>
      )}
      {actionError && (
        <div style={{ padding: '12px 16px', backgroundColor: '#fee2e2', color: '#991b1b', border: '1px solid #fecaca', borderRadius: '6px', marginBottom: '16px' }}>
          {actionError}
        </div>
      )}

      {/* Dashboard Top Cards */}
      {dashboardLoading ? (
        <div style={{ padding: '20px', color: '#64748b' }}>Loading operational metrics...</div>
      ) : dashboardError ? (
        <div style={{ color: '#ef4444', marginBottom: '16px' }}>{dashboardError}</div>
      ) : dashboard ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px', marginBottom: '24px' }}>
          <div style={{ padding: '16px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
            <div style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase' }}>Active Programs</div>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#0f172a' }}>{dashboard.activeProgramsCount}</div>
          </div>
          <div style={{ padding: '16px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
            <div style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase' }}>Upcoming Batches</div>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#0f172a' }}>{dashboard.upcomingBatchesCount}</div>
          </div>
          <div style={{ padding: '16px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
            <div style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase' }}>Occupied / Total Capacity</div>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#0f172a' }}>
              {dashboard.totalOccupiedSeats} / {dashboard.totalConfiguredCapacity}
            </div>
            <div style={{ fontSize: '12px', color: '#16a34a' }}>{dashboard.totalRemainingSeats} seats available</div>
          </div>
          <div style={{ padding: '16px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
            <div style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase' }}>Active Demand</div>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#d97706' }}>{dashboard.activeDemandCount}</div>
          </div>
          <div style={{ padding: '16px', backgroundColor: dashboard.paymentVerifiedExceptionsCount > 0 ? '#fef2f2' : '#f8fafc', border: dashboard.paymentVerifiedExceptionsCount > 0 ? '1px solid #fca5a5' : '1px solid #e2e8f0', borderRadius: '8px' }}>
            <div style={{ fontSize: '12px', color: dashboard.paymentVerifiedExceptionsCount > 0 ? '#991b1b' : '#64748b', textTransform: 'uppercase', fontWeight: 'bold' }}>Payment Exceptions</div>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: dashboard.paymentVerifiedExceptionsCount > 0 ? '#dc2626' : '#0f172a' }}>
              {dashboard.paymentVerifiedExceptionsCount}
            </div>
            <div style={{ fontSize: '12px', color: '#64748b' }}>Verified payments requiring recovery</div>
          </div>
        </div>
      ) : null}

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '1px solid #e2e8f0', marginBottom: '24px' }}>
        <button
          onClick={() => setActiveTab('overview')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'overview' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'overview' ? 'bold' : 'normal', color: activeTab === 'overview' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          Overview & Exceptions
        </button>
        <button
          onClick={() => setActiveTab('programs')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'programs' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'programs' ? 'bold' : 'normal', color: activeTab === 'programs' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          Programs
        </button>
        <button
          onClick={() => setActiveTab('batches')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'batches' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'batches' ? 'bold' : 'normal', color: activeTab === 'batches' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          Batches & Capacity
        </button>
        <button
          onClick={() => setActiveTab('enrollments')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'enrollments' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'enrollments' ? 'bold' : 'normal', color: activeTab === 'enrollments' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          Enrollment Explorer
        </button>
        <button
          onClick={() => setActiveTab('demand')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'demand' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'demand' ? 'bold' : 'normal', color: activeTab === 'demand' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          Demand Monitor
        </button>
      </div>

      {/* Tab 1: Overview & Exceptions */}
      {activeTab === 'overview' && (
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 'bold', color: '#1e293b', marginBottom: '16px' }}>
            Operational Payment Exceptions Console
          </h2>
          {exceptionsLoading ? (
            <div style={{ color: '#64748b' }}>Loading payment exceptions...</div>
          ) : exceptions.length === 0 ? (
            <div style={{ padding: '24px', backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '8px', color: '#166534' }}>
              ✓ All verified payments are in sync with confirmed enrollments. No operational exceptions detected.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#ffffff', border: '1px solid #e2e8f0' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                  <th style={{ padding: '12px' }}>Payment ID</th>
                  <th style={{ padding: '12px' }}>Batch ID</th>
                  <th style={{ padding: '12px' }}>Trainee ID</th>
                  <th style={{ padding: '12px' }}>Enrollment ID</th>
                  <th style={{ padding: '12px' }}>Status</th>
                  <th style={{ padding: '12px' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {exceptions.map((ex) => (
                  <tr key={ex.paymentId} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '12px', fontFamily: 'monospace' }}>{ex.paymentId}</td>
                    <td style={{ padding: '12px' }}>{ex.batchId}</td>
                    <td style={{ padding: '12px' }}>{ex.traineeId}</td>
                    <td style={{ padding: '12px', fontFamily: 'monospace' }}>{ex.enrollmentId || 'N/A'}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: '#fef3c7', color: '#92400e', fontSize: '12px', fontWeight: 'bold' }}>
                        {ex.status}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {ex.enrollmentId ? (
                        <button
                          onClick={() => handleTriggerRecovery(ex.enrollmentId!)}
                          style={{ padding: '6px 12px', backgroundColor: '#2563eb', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                        >
                          Run Recovery
                        </button>
                      ) : (
                        <span style={{ color: '#64748b', fontSize: '12px' }}>No Enrollment Bound</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Tab 2: Programs */}
      {activeTab === 'programs' && <TrainingProgramManagement />}

      {/* Tab 3: Batches & Capacity */}
      {activeTab === 'batches' && <BatchManagementConsole />}

      {/* Tab 4: Enrollment Explorer */}
      {activeTab === 'enrollments' && (
        <div>
          <div style={{ display: 'flex', gap: '12px', marginBottom: '16px' }}>
            <input
              type="text"
              placeholder="Search by Trainee ID or Enrollment Code..."
              value={enrollmentSearch}
              onChange={(e) => { setEnrollmentSearch(e.target.value); setEnrollmentPage(0); }}
              style={{ flex: 1, padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '6px' }}
            />
            <select
              value={enrollmentStatusFilter}
              onChange={(e) => { setEnrollmentStatusFilter(e.target.value); setEnrollmentPage(0); }}
              style={{ padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '6px' }}
            >
              <option value="">All Statuses</option>
              <option value="PENDING">PENDING</option>
              <option value="PAYMENT_PENDING">PAYMENT_PENDING</option>
              <option value="CONFIRMED">CONFIRMED</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="REJECTED">REJECTED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>

          {enrollmentsLoading ? (
            <div style={{ color: '#64748b' }}>Searching enrollments...</div>
          ) : enrollments.length === 0 ? (
            <div style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>No enrollments match the selected criteria.</div>
          ) : (
            <div>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#ffffff', border: '1px solid #e2e8f0' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                    <th style={{ padding: '12px' }}>Code</th>
                    <th style={{ padding: '12px' }}>Batch ID</th>
                    <th style={{ padding: '12px' }}>Trainee ID</th>
                    <th style={{ padding: '12px' }}>Price</th>
                    <th style={{ padding: '12px' }}>Status</th>
                    <th style={{ padding: '12px' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {enrollments.map((e) => (
                    <tr key={e.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                      <td style={{ padding: '12px', fontFamily: 'monospace', fontWeight: 'bold' }}>{e.enrollmentCode || e.id}</td>
                      <td style={{ padding: '12px' }}>{e.batchId}</td>
                      <td style={{ padding: '12px' }}>{e.traineeId}</td>
                      <td style={{ padding: '12px' }}>{e.currency} {e.priceAmount}</td>
                      <td style={{ padding: '12px' }}>
                        <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: e.status === 'CONFIRMED' || e.status === 'ACTIVE' ? '#dcfce7' : e.status === 'PAYMENT_PENDING' ? '#fef3c7' : '#f1f5f9', color: e.status === 'CONFIRMED' || e.status === 'ACTIVE' ? '#15803d' : e.status === 'PAYMENT_PENDING' ? '#b45309' : '#475569', fontSize: '12px', fontWeight: 'bold' }}>
                          {e.status}
                        </span>
                      </td>
                      <td style={{ padding: '12px', display: 'flex', gap: '8px' }}>
                        <button
                          onClick={() => handleViewEnrollmentDetails(e.id)}
                          style={{ padding: '6px 12px', backgroundColor: '#f1f5f9', color: '#1e293b', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer' }}
                        >
                          View Details
                        </button>
                        {(e.status === 'PAYMENT_PENDING' || e.status === 'PENDING') && (
                          <button
                            onClick={() => handleTriggerRecovery(e.id)}
                            style={{ padding: '6px 12px', backgroundColor: '#2563eb', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                          >
                            Recover
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '16px' }}>
                <button
                  disabled={enrollmentPage === 0}
                  onClick={() => setEnrollmentPage(prev => prev - 1)}
                  style={{ padding: '8px 16px', opacity: enrollmentPage === 0 ? 0.5 : 1, cursor: enrollmentPage === 0 ? 'not-allowed' : 'pointer' }}
                >
                  Previous
                </button>
                <span>Page {enrollmentPage + 1} of {enrollmentTotalPages}</span>
                <button
                  disabled={enrollmentPage + 1 >= enrollmentTotalPages}
                  onClick={() => setEnrollmentPage(prev => prev + 1)}
                  style={{ padding: '8px 16px', opacity: enrollmentPage + 1 >= enrollmentTotalPages ? 0.5 : 1, cursor: enrollmentPage + 1 >= enrollmentTotalPages ? 'not-allowed' : 'pointer' }}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 5: Demand Monitor */}
      {activeTab === 'demand' && (
        <div>
          <div style={{ display: 'flex', gap: '12px', marginBottom: '16px' }}>
            <input
              type="text"
              placeholder="Search by Trainee ID or Batch ID..."
              value={demandSearch}
              onChange={(e) => { setDemandSearch(e.target.value); setDemandPage(0); }}
              style={{ flex: 1, padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '6px' }}
            />
            <select
              value={demandStatusFilter}
              onChange={(e) => { setDemandStatusFilter(e.target.value); setDemandPage(0); }}
              style={{ padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '6px' }}
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="RESOLVED">RESOLVED</option>
              <option value="WITHDRAWN">WITHDRAWN</option>
              <option value="EXPIRED">EXPIRED</option>
            </select>
          </div>

          {demandsLoading ? (
            <div style={{ color: '#64748b' }}>Searching demand records...</div>
          ) : demands.length === 0 ? (
            <div style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>No demand records match the selected criteria.</div>
          ) : (
            <div>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#ffffff', border: '1px solid #e2e8f0' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                    <th style={{ padding: '12px' }}>Demand ID</th>
                    <th style={{ padding: '12px' }}>Batch ID</th>
                    <th style={{ padding: '12px' }}>Trainee ID</th>
                    <th style={{ padding: '12px' }}>Status</th>
                    <th style={{ padding: '12px' }}>Requested At</th>
                  </tr>
                </thead>
                <tbody>
                  {demands.map((d) => (
                    <tr key={d.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                      <td style={{ padding: '12px', fontFamily: 'monospace' }}>{d.id}</td>
                      <td style={{ padding: '12px' }}>{d.batchId}</td>
                      <td style={{ padding: '12px' }}>{d.traineeId}</td>
                      <td style={{ padding: '12px' }}>
                        <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: d.status === 'ACTIVE' ? '#fef3c7' : '#f1f5f9', color: d.status === 'ACTIVE' ? '#b45309' : '#475569', fontSize: '12px', fontWeight: 'bold' }}>
                          {d.status}
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>{new Date(d.requestedAt || d.createdAt).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '16px' }}>
                <button
                  disabled={demandPage === 0}
                  onClick={() => setDemandPage(prev => prev - 1)}
                  style={{ padding: '8px 16px', opacity: demandPage === 0 ? 0.5 : 1, cursor: demandPage === 0 ? 'not-allowed' : 'pointer' }}
                >
                  Previous
                </button>
                <span>Page {demandPage + 1} of {demandTotalPages}</span>
                <button
                  disabled={demandPage + 1 >= demandTotalPages}
                  onClick={() => setDemandPage(prev => prev + 1)}
                  style={{ padding: '8px 16px', opacity: demandPage + 1 >= demandTotalPages ? 0.5 : 1, cursor: demandPage + 1 >= demandTotalPages ? 'not-allowed' : 'pointer' }}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Enrollment Details Modal */}
      {selectedEnrollment && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '8px', padding: '24px', maxWidth: '600px', width: '100%', maxHeight: '80vh', overflowY: 'auto' }}>
            <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '16px' }}>
              Enrollment Details ({selectedEnrollment.enrollmentCode || selectedEnrollment.id})
            </h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '16px' }}>
              <div><strong>Batch ID:</strong> {selectedEnrollment.batchId}</div>
              <div><strong>Trainee ID:</strong> {selectedEnrollment.traineeId}</div>
              <div><strong>Status:</strong> {selectedEnrollment.status}</div>
              <div><strong>Price:</strong> {selectedEnrollment.currency} {selectedEnrollment.priceAmount}</div>
              <div><strong>Payment Ref:</strong> {selectedEnrollment.paymentReference || 'None'}</div>
              <div><strong>Confirmed At:</strong> {selectedEnrollment.confirmedAt ? new Date(selectedEnrollment.confirmedAt).toLocaleString() : 'N/A'}</div>
            </div>
            
            <h3 style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '8px' }}>Status Transition History</h3>
            {historyLoading ? (
              <div style={{ color: '#64748b' }}>Loading history...</div>
            ) : enrollmentHistory.length === 0 ? (
              <div style={{ color: '#64748b' }}>No transition history recorded.</div>
            ) : (
              <ul style={{ listStyle: 'none', padding: 0, borderTop: '1px solid #e2e8f0' }}>
                {enrollmentHistory.map((h) => (
                  <li key={h.id} style={{ padding: '8px 0', borderBottom: '1px solid #e2e8f0', fontSize: '14px' }}>
                    <strong>{h.fromStatus || 'INITIAL'} → {h.toStatus}</strong>
                    <div style={{ color: '#64748b', fontSize: '12px' }}>
                      by {h.actor} on {new Date(h.createdAt).toLocaleString()} {h.reason ? `(${h.reason})` : ''}
                    </div>
                  </li>
                ))}
              </ul>
            )}

            <div style={{ textAlign: 'right', marginTop: '20px' }}>
              <button
                onClick={() => setSelectedEnrollment(null)}
                style={{ padding: '8px 16px', backgroundColor: '#e2e8f0', color: '#1e293b', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
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
