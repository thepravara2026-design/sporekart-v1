import React, { useEffect, useState } from 'react';
import {
  TraineeDashboardDto,
  TraineeEnrollmentDetailDto,
  EnrollmentDto,
  DemandDto,
  EnrollmentHistoryDto,
  NotificationDto,
  CertificateDto,
  fetchTraineeDashboard,
  fetchTraineeUpcomingTraining,
  fetchTraineeEnrollmentDetail,
  fetchMyEnrollments,
  fetchMyDemands,
  fetchMyEnrollmentHistory,
  withdrawDemand,
  cancelMyEnrollment,
  rescheduleMyEnrollment,
  fetchTraineeNotifications,
  fetchUnreadNotificationCount,
  markNotificationRead,
  markAllNotificationsRead,
  fetchMyCertificates
} from '../../admin/api/batchApi';

export const TraineeTrainingConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'upcoming' | 'enrollments' | 'demands' | 'notifications' | 'certificates'>('upcoming');

  // Dashboard state
  const [dashboard, setDashboard] = useState<TraineeDashboardDto | null>(null);

  // Upcoming enrollments state
  const [upcoming, setUpcoming] = useState<EnrollmentDto[]>([]);
  const [upcomingLoading, setUpcomingLoading] = useState<boolean>(false);

  // My enrollments state
  const [enrollments, setEnrollments] = useState<EnrollmentDto[]>([]);
  const [enrollmentsLoading, setEnrollmentsLoading] = useState<boolean>(false);
  const [enrollmentPage, setEnrollmentPage] = useState<number>(0);
  const [enrollmentTotalPages, setEnrollmentTotalPages] = useState<number>(1);

  // My demands state
  const [demands, setDemands] = useState<DemandDto[]>([]);
  const [demandsLoading, setDemandsLoading] = useState<boolean>(false);

  // Notifications state
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const [notificationsLoading, setNotificationsLoading] = useState<boolean>(false);
  const [unreadCount, setUnreadCount] = useState<number>(0);

  // Detail Modal state
  const [selectedDetail, setSelectedDetail] = useState<TraineeEnrollmentDetailDto | null>(null);
  const [historyList, setHistoryList] = useState<EnrollmentHistoryDto[]>([]);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);

  // Cancellation / Reschedule Modal state
  const [showCancelForm, setShowCancelForm] = useState<boolean>(false);
  const [cancelReason, setCancelReason] = useState<string>('');
  const [cancelSubmitting, setCancelSubmitting] = useState<boolean>(false);

  const [showRescheduleForm, setShowRescheduleForm] = useState<boolean>(false);
  const [targetBatchId, setTargetBatchId] = useState<string>('');
  const [rescheduleReason, setRescheduleReason] = useState<string>('');
  const [rescheduleSubmitting, setRescheduleSubmitting] = useState<boolean>(false);

  // Certificates state
  const [certificates, setCertificates] = useState<CertificateDto[]>([]);
  const [certificatesLoading, setCertificatesLoading] = useState<boolean>(false);
  const [selectedCertificate, setSelectedCertificate] = useState<CertificateDto | null>(null);

  // Messages
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadDashboard = async () => {
    try {
      const data = await fetchTraineeDashboard();
      setDashboard(data);
    } catch {
      // Quiet fallback
    }
  };

  const loadUpcoming = async () => {
    setUpcomingLoading(true);
    try {
      const data = await fetchTraineeUpcomingTraining();
      setUpcoming(data.content || []);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to load upcoming training';
      setActionError(msg);
    } finally {
      setUpcomingLoading(false);
    }
  };

  const loadEnrollments = async () => {
    setEnrollmentsLoading(true);
    try {
      const data = await fetchMyEnrollments({ page: enrollmentPage, size: 10 });
      setEnrollments(data.content || []);
      setEnrollmentTotalPages(data.totalPages || 1);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to load enrollments';
      setActionError(msg);
    } finally {
      setEnrollmentsLoading(false);
    }
  };

  const loadDemands = async () => {
    setDemandsLoading(true);
    try {
      const data = await fetchMyDemands();
      setDemands(data.content || []);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to load demand requests';
      setActionError(msg);
    } finally {
      setDemandsLoading(false);
    }
  };

  const loadUnreadCount = async () => {
    try {
      const count = await fetchUnreadNotificationCount();
      setUnreadCount(count);
    } catch {
      // Quiet fallback
    }
  };

  const loadNotifications = async () => {
    setNotificationsLoading(true);
    try {
      const data = await fetchTraineeNotifications();
      setNotifications(data.content || []);
      loadUnreadCount();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to load notifications';
      setActionError(msg);
    } finally {
      setNotificationsLoading(false);
    }
  };

  const loadCertificates = async () => {
    setCertificatesLoading(true);
    try {
      const data = await fetchMyCertificates();
      setCertificates(data);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to load earned certificates';
      setActionError(msg);
    } finally {
      setCertificatesLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
    loadUpcoming();
    loadUnreadCount();
  }, []);

  useEffect(() => {
    if (activeTab === 'enrollments') {
      loadEnrollments();
    } else if (activeTab === 'demands') {
      loadDemands();
    } else if (activeTab === 'notifications') {
      loadNotifications();
    } else if (activeTab === 'certificates') {
      loadCertificates();
    }
  }, [activeTab, enrollmentPage]);

  const handleOpenDetail = async (enrollmentId: string) => {
    setDetailLoading(true);
    setSelectedDetail(null);
    setHistoryList([]);
    setActionError(null);
    setShowCancelForm(false);
    setShowRescheduleForm(false);

    try {
      const detail = await fetchTraineeEnrollmentDetail(enrollmentId);
      setSelectedDetail(detail);
      const history = await fetchMyEnrollmentHistory(enrollmentId);
      setHistoryList(history || []);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to fetch enrollment details';
      setActionError(msg);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleWithdrawDemand = async (demandId: string) => {
    if (!window.confirm('Are you sure you want to withdraw this demand request?')) {
      return;
    }
    setActionError(null);
    setActionSuccess(null);
    try {
      await withdrawDemand(demandId);
      setActionSuccess('Demand request successfully withdrawn');
      loadDemands();
      loadDashboard();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to withdraw demand request';
      setActionError(msg);
    }
  };

  const handleExecuteCancel = async () => {
    if (!selectedDetail) return;
    setCancelSubmitting(true);
    setActionError(null);
    setActionSuccess(null);

    try {
      await cancelMyEnrollment(selectedDetail.id, cancelReason);
      setActionSuccess(`Enrollment ${selectedDetail.enrollmentCode || selectedDetail.id} successfully cancelled`);
      setShowCancelForm(false);
      setSelectedDetail(null);
      loadDashboard();
      loadUpcoming();
      loadUnreadCount();
      if (activeTab === 'enrollments') loadEnrollments();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to cancel enrollment. Note: Trainees may only cancel up to 2 days before training starts.';
      setActionError(msg);
    } finally {
      setCancelSubmitting(false);
    }
  };

  const handleExecuteReschedule = async () => {
    if (!selectedDetail || !targetBatchId.trim()) return;
    setRescheduleSubmitting(true);
    setActionError(null);
    setActionSuccess(null);

    try {
      await rescheduleMyEnrollment(selectedDetail.id, targetBatchId.trim(), rescheduleReason);
      setActionSuccess(`Enrollment ${selectedDetail.enrollmentCode || selectedDetail.id} successfully rescheduled to target batch`);
      setShowRescheduleForm(false);
      setSelectedDetail(null);
      loadDashboard();
      loadUpcoming();
      loadUnreadCount();
      if (activeTab === 'enrollments') loadEnrollments();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to reschedule enrollment. Note: Trainees may only reschedule up to 2 days before training starts.';
      setActionError(msg);
    } finally {
      setRescheduleSubmitting(false);
    }
  };

  const handleMarkNotificationRead = async (notificationId: string) => {
    try {
      await markNotificationRead(notificationId);
      loadNotifications();
    } catch {
      // Quiet fail
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllNotificationsRead();
      loadNotifications();
    } catch {
      // Quiet fail
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '24px', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      {/* Header with Notification Bell */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '28px', fontWeight: 'bold', color: '#0f172a', margin: '0 0 8px 0' }}>
            My Training Portal
          </h1>
          <p style={{ color: '#64748b', margin: 0 }}>
            Manage your enrolled training programs, upcoming live sessions, demand requests, and notifications.
          </p>
        </div>
        <button
          onClick={() => setActiveTab('notifications')}
          style={{ position: 'relative', padding: '8px 16px', backgroundColor: '#f1f5f9', border: '1px solid #cbd5e1', borderRadius: '6px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '600', fontSize: '14px' }}
        >
          🔔 Notifications
          {unreadCount > 0 && (
            <span style={{ backgroundColor: '#dc2626', color: '#ffffff', fontSize: '12px', fontWeight: 'bold', padding: '2px 6px', borderRadius: '10px' }}>
              {unreadCount}
            </span>
          )}
        </button>
      </div>

      {/* Global Alerts */}
      {actionSuccess && (
        <div style={{ padding: '12px 16px', backgroundColor: '#ecfdf5', border: '1px solid #a7f3d0', color: '#065f46', borderRadius: '6px', marginBottom: '16px' }}>
          {actionSuccess}
        </div>
      )}
      {actionError && (
        <div style={{ padding: '12px 16px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', borderRadius: '6px', marginBottom: '16px' }}>
          {actionError}
        </div>
      )}

      {/* Summary Cards Row */}
      {dashboard && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '24px' }}>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', padding: '18px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)', transition: 'transform 0.2s ease, box-shadow 0.2s ease', cursor: 'default' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 10px 20px -6px rgba(15, 23, 42, 0.15)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)'; }}>
            <div style={{ fontSize: '12px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Upcoming Trainings</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#0f172a', marginTop: '6px', borderTop: '2px solid #10b981', paddingTop: '8px' }}>{dashboard.upcomingEnrollmentsCount}</div>
          </div>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', padding: '18px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)', transition: 'transform 0.2s ease, box-shadow 0.2s ease' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 10px 20px -6px rgba(15, 23, 42, 0.15)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)'; }}>
            <div style={{ fontSize: '12px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Pending Confirmation</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#d97706', marginTop: '6px', borderTop: '2px solid #f59e0b', paddingTop: '8px' }}>{dashboard.pendingEnrollmentsCount}</div>
          </div>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', padding: '18px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)', transition: 'transform 0.2s ease, box-shadow 0.2s ease' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 10px 20px -6px rgba(15, 23, 42, 0.15)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)'; }}>
            <div style={{ fontSize: '12px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Completed Trainings</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#16a34a', marginTop: '6px', borderTop: '2px solid #22c55e', paddingTop: '8px' }}>{dashboard.completedEnrollmentsCount}</div>
          </div>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', padding: '18px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)', transition: 'transform 0.2s ease, box-shadow 0.2s ease' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 10px 20px -6px rgba(15, 23, 42, 0.15)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)'; }}>
            <div style={{ fontSize: '12px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Active Demand Requests</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#2563eb', marginTop: '6px', borderTop: '2px solid #3b82f6', paddingTop: '8px' }}>{dashboard.activeDemandRequestsCount}</div>
          </div>
        </div>
      )}

      {/* Next Class Hero Banner */}
      {dashboard && dashboard.nextUpcomingSessionTitle && (
        <div style={{ backgroundColor: '#1e1b4b', color: '#ffffff', borderRadius: '8px', padding: '20px 24px', marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ fontSize: '12px', fontWeight: 'bold', textTransform: 'uppercase', color: '#818cf8', letterSpacing: '0.05em' }}>Your Next Class Session</div>
            <div style={{ fontSize: '20px', fontWeight: 'bold', marginTop: '4px' }}>{dashboard.nextUpcomingSessionTitle}</div>
            <div style={{ fontSize: '14px', color: '#c7d2fe', marginTop: '4px' }}>
              Batch: {dashboard.nextUpcomingBatchCode} • Start: {dashboard.nextUpcomingStartDate ? new Date(dashboard.nextUpcomingStartDate).toLocaleString() : 'TBD'}
            </div>
          </div>
          {dashboard.nextUpcomingVenueOrMeeting && (
            <a
              href={dashboard.nextUpcomingVenueOrMeeting}
              target="_blank"
              rel="noreferrer"
              style={{ backgroundColor: '#4f46e5', color: '#ffffff', textDecoration: 'none', padding: '10px 20px', borderRadius: '6px', fontWeight: 'bold', fontSize: '14px', display: 'inline-block' }}
            >
              Join Live Meeting →
            </a>
          )}
        </div>
      )}

      {/* Console Tabs */}
      <div style={{ borderBottom: '1px solid #e2e8f0', marginBottom: '20px', display: 'flex', gap: '24px' }}>
        <button
          onClick={() => setActiveTab('upcoming')}
          style={{ padding: '12px 4px', border: 'none', background: 'none', fontWeight: 'bold', fontSize: '15px', cursor: 'pointer', borderBottom: activeTab === 'upcoming' ? '2px solid #2563eb' : 'none', color: activeTab === 'upcoming' ? '#2563eb' : '#64748b' }}
        >
          Upcoming Training
        </button>
        <button
          onClick={() => setActiveTab('enrollments')}
          style={{ padding: '12px 4px', border: 'none', background: 'none', fontWeight: 'bold', fontSize: '15px', cursor: 'pointer', borderBottom: activeTab === 'enrollments' ? '2px solid #2563eb' : 'none', color: activeTab === 'enrollments' ? '#2563eb' : '#64748b' }}
        >
          All My Enrollments
        </button>
        <button
          onClick={() => setActiveTab('demands')}
          style={{ padding: '12px 4px', border: 'none', background: 'none', fontWeight: 'bold', fontSize: '15px', cursor: 'pointer', borderBottom: activeTab === 'demands' ? '2px solid #2563eb' : 'none', color: activeTab === 'demands' ? '#2563eb' : '#64748b' }}
        >
          My Demand Requests
        </button>
        <button
          onClick={() => setActiveTab('notifications')}
          style={{ padding: '12px 4px', border: 'none', background: 'none', fontWeight: 'bold', fontSize: '15px', cursor: 'pointer', borderBottom: activeTab === 'notifications' ? '2px solid #2563eb' : 'none', color: activeTab === 'notifications' ? '#2563eb' : '#64748b' }}
        >
          Notifications {unreadCount > 0 && `(${unreadCount})`}
        </button>
        <button
          onClick={() => setActiveTab('certificates')}
          style={{ padding: '12px 4px', border: 'none', background: 'none', fontWeight: 'bold', fontSize: '15px', cursor: 'pointer', borderBottom: activeTab === 'certificates' ? '2px solid #2563eb' : 'none', color: activeTab === 'certificates' ? '#2563eb' : '#64748b' }}
        >
          Certificates & Achievements 🎓
        </button>
      </div>

      {/* Tab Contents */}
      {activeTab === 'upcoming' && (
        <div>
          {upcomingLoading ? (
            <div style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>Loading upcoming training sessions...</div>
          ) : upcoming.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', color: '#64748b' }}>
              No upcoming training sessions scheduled at this time.
            </div>
          ) : (
            <div style={{ display: 'grid', gap: '16px' }}>
              {upcoming.map((e) => (
                <div key={e.id} style={{ backgroundColor: '#ffffff', borderRadius: '12px', padding: '18px 20px', border: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px', flexWrap: 'wrap', boxShadow: '0 1px 3px rgba(0,0,0,0.04)', borderLeft: '4px solid #10b981', transition: 'transform 0.2s ease, box-shadow 0.2s ease' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 12px 24px -8px rgba(15, 23, 42, 0.18)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.04)'; }}>
                  <div>
                    <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#0f172a' }}>
                      Batch: {e.batchCode || e.batchId}
                    </div>
                    <div style={{ fontSize: '13px', color: '#64748b', marginTop: '4px' }}>
                      Enrollment Code: {e.enrollmentCode || e.id} • Status: <span style={{ fontWeight: '600', color: e.status === 'CONFIRMED' ? '#16a34a' : '#d97706' }}>{e.status}</span>
                    </div>
                  </div>
                  <button
                    onClick={() => handleOpenDetail(e.id)}
                    style={{ padding: '8px 16px', backgroundColor: '#f1f5f9', color: '#0f172a', border: '1px solid #cbd5e1', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', fontSize: '13px', transition: 'background-color 0.15s ease, border-color 0.15s ease' }}
                    onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = '#e2e8f0'; e.currentTarget.style.borderColor = '#94a3b8'; }}
                    onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = '#f1f5f9'; e.currentTarget.style.borderColor = '#cbd5e1'; }}
                  >
                    View Details & Actions
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'enrollments' && (
        <div>
          {enrollmentsLoading ? (
            <div style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>Loading enrollments...</div>
          ) : (
            <div>
              <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#ffffff', borderRadius: '8px', overflow: 'hidden', border: '1px solid #e2e8f0' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8fafc', textTransform: 'uppercase', fontSize: '12px', color: '#64748b', textAlign: 'left' }}>
                    <th style={{ padding: '12px 16px' }}>Enrollment Code</th>
                    <th style={{ padding: '12px 16px' }}>Batch ID</th>
                    <th style={{ padding: '12px 16px' }}>Status</th>
                    <th style={{ padding: '12px 16px' }}>Price</th>
                    <th style={{ padding: '12px 16px' }}>Enrolled Date</th>
                    <th style={{ padding: '12px 16px', textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {enrollments.map((e) => (
                    <tr key={e.id} style={{ borderTop: '1px solid #e2e8f0', fontSize: '14px' }}>
                      <td style={{ padding: '12px 16px', fontWeight: '600' }}>{e.enrollmentCode || e.id}</td>
                      <td style={{ padding: '12px 16px' }}>{e.batchCode || e.batchId}</td>
                      <td style={{ padding: '12px 16px' }}>
                        <span style={{ padding: '4px 8px', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold', backgroundColor: e.status === 'CONFIRMED' ? '#dcfce7' : e.status === 'CANCELLED' ? '#fee2e2' : '#fef3c7', color: e.status === 'CONFIRMED' ? '#166534' : e.status === 'CANCELLED' ? '#991b1b' : '#92400e' }}>
                          {e.status}
                        </span>
                      </td>
                      <td style={{ padding: '12px 16px' }}>{e.currency || 'INR'} {e.priceAmount || 0}</td>
                      <td style={{ padding: '12px 16px', color: '#64748b' }}>{new Date(e.enrolledAt).toLocaleDateString()}</td>
                      <td style={{ padding: '12px 16px', textAlign: 'right' }}>
                        <button
                          onClick={() => handleOpenDetail(e.id)}
                          style={{ padding: '6px 12px', backgroundColor: '#f1f5f9', color: '#0f172a', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: '600' }}
                        >
                          Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination */}
              <div style={{ marginTop: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <button
                  disabled={enrollmentPage === 0}
                  onClick={() => setEnrollmentPage(p => Math.max(0, p - 1))}
                  style={{ padding: '6px 16px', border: '1px solid #cbd5e1', borderRadius: '4px', backgroundColor: '#ffffff', cursor: enrollmentPage === 0 ? 'not-allowed' : 'pointer' }}
                >
                  Previous
                </button>
                <span style={{ fontSize: '14px', color: '#64748b' }}>
                  Page {enrollmentPage + 1} of {enrollmentTotalPages}
                </span>
                <button
                  disabled={enrollmentPage >= enrollmentTotalPages - 1}
                  onClick={() => setEnrollmentPage(p => p + 1)}
                  style={{ padding: '6px 16px', border: '1px solid #cbd5e1', borderRadius: '4px', backgroundColor: '#ffffff', cursor: enrollmentPage >= enrollmentTotalPages - 1 ? 'not-allowed' : 'pointer' }}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'demands' && (
        <div>
          {demandsLoading ? (
            <div style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>Loading demand requests...</div>
          ) : demands.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', color: '#64748b' }}>
              No active or historical demand requests found.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#ffffff', borderRadius: '8px', overflow: 'hidden', border: '1px solid #e2e8f0' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', textTransform: 'uppercase', fontSize: '12px', color: '#64748b', textAlign: 'left' }}>
                  <th style={{ padding: '12px 16px' }}>Demand ID</th>
                  <th style={{ padding: '12px 16px' }}>Batch Code</th>
                  <th style={{ padding: '12px 16px' }}>Status</th>
                  <th style={{ padding: '12px 16px' }}>Requested At</th>
                  <th style={{ padding: '12px 16px', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {demands.map((d) => (
                  <tr key={d.id} style={{ borderTop: '1px solid #e2e8f0', fontSize: '14px' }}>
                    <td style={{ padding: '12px 16px', fontWeight: '600' }}>{d.id}</td>
                    <td style={{ padding: '12px 16px' }}>{d.batchCode || d.batchId}</td>
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold', backgroundColor: d.status === 'ACTIVE' ? '#dbeafe' : '#f1f5f9', color: d.status === 'ACTIVE' ? '#1e40af' : '#475569' }}>
                        {d.status}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px', color: '#64748b' }}>{new Date(d.createdAt).toLocaleDateString()}</td>
                    <td style={{ padding: '12px 16px', textAlign: 'right' }}>
                      {d.status === 'ACTIVE' && (
                        <button
                          onClick={() => handleWithdrawDemand(d.id)}
                          style={{ padding: '6px 12px', backgroundColor: '#fee2e2', color: '#991b1b', border: '1px solid #fca5a5', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: '600' }}
                        >
                          Withdraw Request
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {activeTab === 'notifications' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 'bold', color: '#0f172a', margin: 0 }}>In-App Notifications</h2>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllRead}
                style={{ padding: '6px 12px', backgroundColor: '#f1f5f9', color: '#0f172a', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer', fontSize: '13px', fontWeight: '600' }}
              >
                Mark All as Read
              </button>
            )}
          </div>

          {notificationsLoading ? (
            <div style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>Loading notifications...</div>
          ) : notifications.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', color: '#64748b' }}>
              No notifications found in your inbox.
            </div>
          ) : (
            <div style={{ display: 'grid', gap: '12px' }}>
              {notifications.map((n) => (
                <div
                  key={n.id}
                  style={{
                    backgroundColor: n.read ? '#ffffff' : '#f0f9ff',
                    border: '1px solid #e2e8f0',
                    borderLeft: n.read ? '1px solid #e2e8f0' : '4px solid #0284c7',
                    borderRadius: '8px',
                    padding: '16px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'flex-start'
                  }}
                >
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                      <span style={{ fontSize: '14px', fontWeight: 'bold', color: '#0f172a' }}>{n.subject}</span>
                      <span style={{ fontSize: '11px', fontWeight: 'bold', padding: '2px 6px', borderRadius: '4px', backgroundColor: '#e2e8f0', color: '#475569' }}>
                        {n.eventType}
                      </span>
                    </div>
                    <p style={{ margin: '4px 0 8px 0', fontSize: '14px', color: '#334155' }}>{n.body}</p>
                    <div style={{ fontSize: '12px', color: '#94a3b8' }}>
                      Received: {new Date(n.createdAt).toLocaleString()}
                    </div>
                  </div>
                  {!n.read && (
                    <button
                      onClick={() => handleMarkNotificationRead(n.id)}
                      style={{ padding: '4px 10px', backgroundColor: '#e0f2fe', color: '#0369a1', border: '1px solid #bae6fd', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: '600' }}
                    >
                      Mark Read
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'certificates' && (
        <div>
          <h2 style={{ fontSize: '18px', fontWeight: 'bold', color: '#0f172a', marginBottom: '16px' }}>Earned Digital Certificates 🎓</h2>
          {certificatesLoading ? (
            <div style={{ padding: '20px', color: '#64748b' }}>Loading digital certificates...</div>
          ) : certificates.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px', color: '#64748b' }}>
              No earned certificates yet. Complete a training program with 80%+ attendance to receive your verified digital certificate.
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' }}>
              {certificates.map(cert => (
                <div key={cert.id} style={{ border: '1px solid #cbd5e1', borderRadius: '8px', padding: '16px', backgroundColor: '#ffffff', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                  <div style={{ fontSize: '11px', fontWeight: 'bold', color: '#2563eb', textTransform: 'uppercase', marginBottom: '4px' }}>VERIFIED CERTIFICATE</div>
                  <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#0f172a', margin: '0 0 8px 0' }}>{cert.programTitle}</h3>
                  <div style={{ fontSize: '13px', color: '#475569', marginBottom: '4px' }}>Batch: <strong>{cert.batchCode}</strong></div>
                  <div style={{ fontSize: '13px', color: '#475569', marginBottom: '4px' }}>Cert #: <code style={{ backgroundColor: '#f1f5f9', padding: '2px 6px', borderRadius: '4px' }}>{cert.certificateNumber}</code></div>
                  <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '12px' }}>Issued: {new Date(cert.issuedAt).toLocaleDateString()}</div>
                  <button
                    onClick={() => setSelectedCertificate(cert)}
                    style={{ width: '100%', padding: '8px', backgroundColor: '#2563eb', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '13px' }}
                  >
                    View Digital Certificate 📜
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Certificate Viewer Modal */}
      {selectedCertificate && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', padding: '32px', maxWidth: '650px', width: '100%', border: '4px double #2563eb', boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)' }}>
            <div style={{ textAlign: 'center', borderBottom: '2px solid #e2e8f0', paddingBottom: '16px', marginBottom: '20px' }}>
              <h1 style={{ fontSize: '24px', fontWeight: 'bold', color: '#1e3a8a', margin: '0 0 4px 0', letterSpacing: '1px' }}>CERTIFICATE OF COMPLETION</h1>
              <div style={{ fontSize: '13px', color: '#64748b', textTransform: 'uppercase', letterSpacing: '2px' }}>SPOREKART ACADEMY & TRAINING AUTHORITY</div>
            </div>

            <div style={{ textAlign: 'center', margin: '24px 0' }}>
              <div style={{ fontSize: '14px', color: '#475569', marginBottom: '8px' }}>This is to certify that</div>
              <h2 style={{ fontSize: '26px', fontWeight: 'bold', color: '#0f172a', margin: '0 0 12px 0', textDecoration: 'underline' }}>{selectedCertificate.traineeName}</h2>
              <div style={{ fontSize: '14px', color: '#475569', marginBottom: '8px' }}>has successfully completed the training program</div>
              <h3 style={{ fontSize: '20px', fontWeight: 'bold', color: '#2563eb', margin: '0 0 16px 0' }}>{selectedCertificate.programTitle}</h3>
              <div style={{ fontSize: '13px', color: '#64748b' }}>Batch: <strong>{selectedCertificate.batchCode}</strong> | Date: <strong>{new Date(selectedCertificate.completionDate).toLocaleDateString()}</strong></div>
            </div>

            <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0', marginTop: '24px', fontSize: '12px', color: '#475569' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                <span>Certificate Number: <strong>{selectedCertificate.certificateNumber}</strong></span>
                <span>Status: <strong style={{ color: selectedCertificate.revoked ? '#dc2626' : '#16a34a' }}>{selectedCertificate.revoked ? 'REVOKED' : 'VERIFIED VALID'}</strong></span>
              </div>
              <div style={{ marginBottom: '8px' }}>Verification Hash: <code style={{ backgroundColor: '#e2e8f0', padding: '2px 4px', borderRadius: '3px' }}>{selectedCertificate.verificationCode}</code></div>
              <div style={{ fontSize: '11px', color: '#64748b' }}>
                Public verification URL: <a href={`/api/v1/certificates/verify/${selectedCertificate.verificationCode}`} target="_blank" rel="noreferrer" style={{ color: '#2563eb' }}>/api/v1/certificates/verify/{selectedCertificate.verificationCode}</a>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '24px' }}>
              <div style={{ fontSize: '12px', color: '#64748b', fontStyle: 'italic' }}>
                Issued by: {selectedCertificate.issuerSignature}
              </div>
              <button
                onClick={() => setSelectedCertificate(null)}
                style={{ padding: '8px 24px', backgroundColor: '#0f172a', color: '#ffffff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}
              >
                Close Certificate
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Enrollment Detail & Action Modal */}
      {(selectedDetail || detailLoading) && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '8px', padding: '24px', maxWidth: '650px', width: '100%', maxHeight: '85vh', overflowY: 'auto' }}>
            {detailLoading ? (
              <div style={{ padding: '20px', color: '#64748b' }}>Loading enrollment details...</div>
            ) : selectedDetail ? (
              <div>
                <h2 style={{ fontSize: '22px', fontWeight: 'bold', color: '#0f172a', marginBottom: '8px' }}>
                  {selectedDetail.programTitle}
                </h2>
                <div style={{ fontSize: '14px', color: '#64748b', marginBottom: '16px' }}>
                  Category: {selectedDetail.programCategory} • Batch Code: {selectedDetail.batchCode} ({selectedDetail.deliveryMode})
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', backgroundColor: '#f8fafc', padding: '16px', borderRadius: '6px', marginBottom: '16px' }}>
                  <div><strong>Enrollment Code:</strong> {selectedDetail.enrollmentCode || selectedDetail.id}</div>
                  <div><strong>Enrollment Status:</strong> {selectedDetail.enrollmentStatus}</div>
                  <div><strong>Payment Status:</strong> {selectedDetail.paymentStatusSummary}</div>
                  <div><strong>Price:</strong> {selectedDetail.currency} {selectedDetail.priceAmount}</div>
                  <div><strong>Payment Ref:</strong> {selectedDetail.paymentReference || 'None'}</div>
                  <div><strong>Timezone:</strong> {selectedDetail.timezone}</div>
                </div>

                {selectedDetail.meetingUrl && (
                  <div style={{ padding: '12px 16px', backgroundColor: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '6px', marginBottom: '16px' }}>
                    <strong>Online Meeting Link:</strong>{' '}
                    <a href={selectedDetail.meetingUrl} target="_blank" rel="noreferrer" style={{ color: '#2563eb', fontWeight: 'bold' }}>
                      {selectedDetail.meetingUrl}
                    </a>
                  </div>
                )}

                {selectedDetail.venueInfo && (
                  <div style={{ padding: '12px 16px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '6px', marginBottom: '16px' }}>
                    <strong>Venue Information:</strong> {selectedDetail.venueInfo}
                  </div>
                )}

                <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#1e293b', marginBottom: '8px' }}>Training Schedule Sessions</h3>
                {selectedDetail.schedules && selectedDetail.schedules.length > 0 ? (
                  <ul style={{ listStyle: 'none', padding: 0, borderTop: '1px solid #e2e8f0', marginBottom: '16px' }}>
                    {selectedDetail.schedules.map((s) => (
                      <li key={s.id} style={{ padding: '10px 0', borderBottom: '1px solid #e2e8f0', fontSize: '14px' }}>
                        <strong>{s.title}</strong>
                        <div style={{ color: '#64748b', fontSize: '12px' }}>
                          Scheduled: {new Date(s.scheduledAt).toLocaleString()} ({s.durationMinutes} mins) {s.location ? `• Location: ${s.location}` : ''}
                        </div>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div style={{ color: '#64748b', fontSize: '14px', marginBottom: '16px' }}>No specific schedule sessions declared for this batch.</div>
                )}

                {historyList && historyList.length > 0 && (
                  <div style={{ marginBottom: '16px' }}>
                    <h3 style={{ fontSize: '15px', fontWeight: 'bold', color: '#1e293b', marginBottom: '8px' }}>Enrollment History Trail</h3>
                    <ul style={{ listStyle: 'none', padding: 0, fontSize: '13px', color: '#475569' }}>
                      {historyList.map(h => (
                        <li key={h.id} style={{ padding: '4px 0', borderBottom: '1px dashed #e2e8f0' }}>
                          [{new Date(h.createdAt).toLocaleTimeString()}] Status changed to <strong>{h.toStatus}</strong> by {h.actor} {h.reason ? `(${h.reason})` : ''}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Cancel & Reschedule Action Forms */}
                {showCancelForm ? (
                  <div style={{ padding: '16px', backgroundColor: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '6px', marginBottom: '16px' }}>
                    <h4 style={{ margin: '0 0 8px 0', color: '#991b1b', fontSize: '15px', fontWeight: 'bold' }}>Confirm Cancellation</h4>
                    <p style={{ fontSize: '13px', color: '#7f1d1d', margin: '0 0 12px 0' }}>
                      Note: Trainee cancellation policy allows cancellation up to <strong>2 days before the scheduled training start date</strong>.
                    </p>
                    <input
                      type="text"
                      placeholder="Reason for cancellation (optional)"
                      value={cancelReason}
                      onChange={(e) => setCancelReason(e.target.value)}
                      style={{ width: '100%', padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '4px', marginBottom: '12px', fontSize: '14px' }}
                    />
                    <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                      <button
                        onClick={() => setShowCancelForm(false)}
                        disabled={cancelSubmitting}
                        style={{ padding: '6px 14px', backgroundColor: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer' }}
                      >
                        Back
                      </button>
                      <button
                        onClick={handleExecuteCancel}
                        disabled={cancelSubmitting}
                        style={{ padding: '6px 14px', backgroundColor: '#dc2626', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                      >
                        {cancelSubmitting ? 'Cancelling...' : 'Confirm Cancel'}
                      </button>
                    </div>
                  </div>
                ) : showRescheduleForm ? (
                  <div style={{ padding: '16px', backgroundColor: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '6px', marginBottom: '16px' }}>
                    <h4 style={{ margin: '0 0 8px 0', color: '#1e40af', fontSize: '15px', fontWeight: 'bold' }}>Reschedule Training Enrollment</h4>
                    <p style={{ fontSize: '13px', color: '#1e3a8a', margin: '0 0 12px 0' }}>
                      Note: Trainee rescheduling policy allows rescheduling up to <strong>2 days before the scheduled start date</strong> into compatible available batches.
                    </p>
                    <input
                      type="text"
                      placeholder="Target Batch ID (e.g., batch-target-002)"
                      value={targetBatchId}
                      onChange={(e) => setTargetBatchId(e.target.value)}
                      style={{ width: '100%', padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '4px', marginBottom: '8px', fontSize: '14px' }}
                    />
                    <input
                      type="text"
                      placeholder="Reason for rescheduling (optional)"
                      value={rescheduleReason}
                      onChange={(e) => setRescheduleReason(e.target.value)}
                      style={{ width: '100%', padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '4px', marginBottom: '12px', fontSize: '14px' }}
                    />
                    <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                      <button
                        onClick={() => setShowRescheduleForm(false)}
                        disabled={rescheduleSubmitting}
                        style={{ padding: '6px 14px', backgroundColor: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer' }}
                      >
                        Back
                      </button>
                      <button
                        onClick={handleExecuteReschedule}
                        disabled={rescheduleSubmitting || !targetBatchId.trim()}
                        style={{ padding: '6px 14px', backgroundColor: '#2563eb', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                      >
                        {rescheduleSubmitting ? 'Rescheduling...' : 'Confirm Reschedule'}
                      </button>
                    </div>
                  </div>
                ) : null}

                {/* Bottom Modal Actions */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #e2e8f0', paddingTop: '16px' }}>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    {selectedDetail.enrollmentStatus !== 'CANCELLED' && selectedDetail.enrollmentStatus !== 'COMPLETED' && !showCancelForm && !showRescheduleForm && (
                      <>
                        <button
                          onClick={() => { setShowCancelForm(true); setShowRescheduleForm(false); }}
                          style={{ padding: '8px 16px', backgroundColor: '#fee2e2', color: '#991b1b', border: '1px solid #fca5a5', borderRadius: '4px', cursor: 'pointer', fontWeight: '600', fontSize: '13px' }}
                        >
                          Cancel Training
                        </button>
                        <button
                          onClick={() => { setShowRescheduleForm(true); setShowCancelForm(false); }}
                          style={{ padding: '8px 16px', backgroundColor: '#dbeafe', color: '#1e40af', border: '1px solid #93c5fd', borderRadius: '4px', cursor: 'pointer', fontWeight: '600', fontSize: '13px' }}
                        >
                          Reschedule Training
                        </button>
                      </>
                    )}
                  </div>
                  <button
                    onClick={() => { setSelectedDetail(null); setShowCancelForm(false); setShowRescheduleForm(false); }}
                    style={{ padding: '8px 20px', backgroundColor: '#0f172a', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '13px' }}
                  >
                    Close
                  </button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}
    </div>
  );
};
