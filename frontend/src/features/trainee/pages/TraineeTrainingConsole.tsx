import React, { useEffect, useState } from 'react';
import {
  TraineeDashboardDto,
  TraineeEnrollmentDetailDto,
  EnrollmentDto,
  DemandDto,
  EnrollmentHistoryDto,
  NotificationDto,
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
  markAllNotificationsRead
} from '../../admin/api/batchApi';

export const TraineeTrainingConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'upcoming' | 'enrollments' | 'demands' | 'notifications'>('upcoming');

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

  // Messages
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadDashboard = async () => {
    try {
      const data = await fetchTraineeDashboard();
      setDashboard(data);
    } catch (err: any) {
      // Quiet fail if guest
    }
  };

  const loadUnreadCount = async () => {
    try {
      const count = await fetchUnreadNotificationCount();
      setUnreadCount(count);
    } catch (err: any) {
      // Quiet fail
    }
  };

  const loadUpcoming = async () => {
    setUpcomingLoading(true);
    try {
      const data = await fetchTraineeUpcomingTraining();
      setUpcoming(data.content || []);
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to load upcoming training');
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
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to load enrollments');
    } finally {
      setEnrollmentsLoading(false);
    }
  };

  const loadDemands = async () => {
    setDemandsLoading(true);
    try {
      const data = await fetchMyDemands();
      setDemands(data.content || []);
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to load demand requests');
    } finally {
      setDemandsLoading(false);
    }
  };

  const loadNotifications = async () => {
    setNotificationsLoading(true);
    try {
      const data = await fetchTraineeNotifications();
      setNotifications(data.content || []);
      loadUnreadCount();
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to load notifications');
    } finally {
      setNotificationsLoading(false);
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
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to fetch enrollment details');
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
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to withdraw demand request');
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
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to cancel enrollment. Note: Trainees may only cancel up to 2 days before training starts.');
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
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to reschedule enrollment. Note: Trainees may only reschedule up to 2 days before training starts.');
    } finally {
      setRescheduleSubmitting(false);
    }
  };

  const handleMarkNotificationRead = async (notificationId: string) => {
    try {
      await markNotificationRead(notificationId);
      loadNotifications();
    } catch (err: any) {
      // Quiet fail
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllNotificationsRead();
      loadNotifications();
    } catch (err: any) {
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
          <div style={{ backgroundColor: '#ffffff', borderRadius: '8px', padding: '16px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ fontSize: '13px', color: '#64748b', fontWeight: '600', textTransform: 'uppercase' }}>Upcoming Trainings</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#0f172a', marginTop: '4px' }}>{dashboard.upcomingEnrollmentsCount}</div>
          </div>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '8px', padding: '16px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ fontSize: '13px', color: '#64748b', fontWeight: '600', textTransform: 'uppercase' }}>Pending Confirmation</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#d97706', marginTop: '4px' }}>{dashboard.pendingEnrollmentsCount}</div>
          </div>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '8px', padding: '16px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ fontSize: '13px', color: '#64748b', fontWeight: '600', textTransform: 'uppercase' }}>Completed Trainings</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#16a34a', marginTop: '4px' }}>{dashboard.completedEnrollmentsCount}</div>
          </div>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '8px', padding: '16px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ fontSize: '13px', color: '#64748b', fontWeight: '600', textTransform: 'uppercase' }}>Active Demand Requests</div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#2563eb', marginTop: '4px' }}>{dashboard.activeDemandRequestsCount}</div>
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
                <div key={e.id} style={{ backgroundColor: '#ffffff', borderRadius: '8px', padding: '16px 20px', border: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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
                    style={{ padding: '8px 16px', backgroundColor: '#f1f5f9', color: '#0f172a', border: '1px solid #cbd5e1', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', fontSize: '13px' }}
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
                      <td style={{ padding: '12px 16px' }}>{e.currency || 'USD'} {e.priceAmount || 0}</td>
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
