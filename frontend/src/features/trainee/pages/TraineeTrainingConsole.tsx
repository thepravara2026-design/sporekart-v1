import React, { useEffect, useState } from 'react';
import {
  TraineeDashboardDto,
  TraineeEnrollmentDetailDto,
  EnrollmentDto,
  DemandDto,
  EnrollmentHistoryDto,
  fetchTraineeDashboard,
  fetchTraineeUpcomingTraining,
  fetchTraineeEnrollmentDetail,
  fetchMyEnrollments,
  fetchMyDemands,
  fetchMyEnrollmentHistory,
  withdrawDemand
} from '../../admin/api/batchApi';

export const TraineeTrainingConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'upcoming' | 'enrollments' | 'demands'>('upcoming');

  // Dashboard state
  const [dashboard, setDashboard] = useState<TraineeDashboardDto | null>(null);
  const [dashboardLoading, setDashboardLoading] = useState<boolean>(true);

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

  // Detail Modal state
  const [selectedDetail, setSelectedDetail] = useState<TraineeEnrollmentDetailDto | null>(null);
  const [historyList, setHistoryList] = useState<EnrollmentHistoryDto[]>([]);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);

  // Messages
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadDashboard = async () => {
    setDashboardLoading(true);
    try {
      const data = await fetchTraineeDashboard();
      setDashboard(data);
    } catch (err: any) {
      // Quiet fail if guest or error
    } finally {
      setDashboardLoading(false);
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

  useEffect(() => {
    loadDashboard();
    loadUpcoming();
  }, []);

  useEffect(() => {
    if (activeTab === 'enrollments') loadEnrollments();
    if (activeTab === 'demands') loadDemands();
  }, [activeTab, enrollmentPage]);

  const handleOpenDetail = async (enrollmentId: string) => {
    setDetailLoading(true);
    setSelectedDetail(null);
    try {
      const detail = await fetchTraineeEnrollmentDetail(enrollmentId);
      setSelectedDetail(detail);
      const hist = await fetchMyEnrollmentHistory(enrollmentId);
      setHistoryList(hist);
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to fetch enrollment details');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleWithdrawDemand = async (demandId: string) => {
    setActionSuccess(null);
    setActionError(null);
    try {
      await withdrawDemand(demandId);
      setActionSuccess('Successfully withdrawn demand request.');
      loadDemands();
      loadDashboard();
    } catch (err: any) {
      setActionError(err?.response?.data?.message || 'Failed to withdraw demand request');
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto', fontFamily: 'sans-serif' }}>
      <header style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', color: '#0f172a', marginBottom: '8px' }}>
          My Training Portal
        </h1>
        <p style={{ color: '#64748b' }}>
          View and manage your enrolled training sessions, schedules, payment status, and requests.
        </p>
      </header>

      {/* Messages */}
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

      {/* Dashboard Summary Cards */}
      {dashboardLoading ? (
        <div style={{ padding: '16px', color: '#64748b' }}>Loading dashboard...</div>
      ) : dashboard ? (
        <div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px', marginBottom: '24px' }}>
            <div style={{ padding: '16px', backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '8px' }}>
              <div style={{ fontSize: '12px', color: '#166534', textTransform: 'uppercase', fontWeight: 'bold' }}>Upcoming / Active</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#15803d' }}>{dashboard.upcomingEnrollmentsCount}</div>
            </div>
            <div style={{ padding: '16px', backgroundColor: '#fef3c7', border: '1px solid #fde68a', borderRadius: '8px' }}>
              <div style={{ fontSize: '12px', color: '#92400e', textTransform: 'uppercase', fontWeight: 'bold' }}>Pending Action</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#b45309' }}>{dashboard.pendingEnrollmentsCount}</div>
            </div>
            <div style={{ padding: '16px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
              <div style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase' }}>Completed Classes</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#0f172a' }}>{dashboard.completedEnrollmentsCount}</div>
            </div>
            <div style={{ padding: '16px', backgroundColor: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '8px' }}>
              <div style={{ fontSize: '12px', color: '#1e40af', textTransform: 'uppercase', fontWeight: 'bold' }}>My Demand Requests</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#1d4ed8' }}>{dashboard.activeDemandRequestsCount}</div>
            </div>
          </div>

          {/* Next Class Highlight Banner */}
          {dashboard.nextUpcomingBatchCode && (
            <div style={{ padding: '20px', backgroundColor: '#1e293b', color: '#ffffff', borderRadius: '8px', marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
              <div>
                <div style={{ fontSize: '12px', color: '#38bdf8', textTransform: 'uppercase', fontWeight: 'bold' }}>Next Session Coming Up</div>
                <h3 style={{ fontSize: '20px', fontWeight: 'bold', margin: '4px 0' }}>{dashboard.nextUpcomingSessionTitle}</h3>
                <div style={{ fontSize: '14px', color: '#cbd5e1' }}>
                  Batch: {dashboard.nextUpcomingBatchCode} • Starts: {new Date(dashboard.nextUpcomingStartDate!).toLocaleString()}
                </div>
              </div>
              <div>
                {dashboard.nextUpcomingVenueOrMeeting ? (
                  <a
                    href={dashboard.nextUpcomingVenueOrMeeting.startsWith('http') ? dashboard.nextUpcomingVenueOrMeeting : '#'}
                    target="_blank"
                    rel="noreferrer"
                    style={{ padding: '10px 20px', backgroundColor: '#0284c7', color: '#ffffff', textDecoration: 'none', borderRadius: '6px', fontWeight: 'bold', display: 'inline-block' }}
                  >
                    {dashboard.nextUpcomingDeliveryMode === 'ONLINE' ? 'Join Online Meeting' : 'View Venue Details'}
                  </a>
                ) : (
                  <span style={{ fontSize: '14px', color: '#94a3b8' }}>Session link pending</span>
                )}
              </div>
            </div>
          )}
        </div>
      ) : null}

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', borderBottom: '1px solid #e2e8f0', marginBottom: '24px' }}>
        <button
          onClick={() => setActiveTab('upcoming')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'upcoming' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'upcoming' ? 'bold' : 'normal', color: activeTab === 'upcoming' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          Upcoming Training
        </button>
        <button
          onClick={() => setActiveTab('enrollments')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'enrollments' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'enrollments' ? 'bold' : 'normal', color: activeTab === 'enrollments' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          All My Enrollments
        </button>
        <button
          onClick={() => setActiveTab('demands')}
          style={{ padding: '12px 20px', border: 'none', borderBottom: activeTab === 'demands' ? '2px solid #2563eb' : 'none', fontWeight: activeTab === 'demands' ? 'bold' : 'normal', color: activeTab === 'demands' ? '#2563eb' : '#64748b', background: 'none', cursor: 'pointer' }}
        >
          My Demand Requests
        </button>
      </div>

      {/* Tab 1: Upcoming */}
      {activeTab === 'upcoming' && (
        <div>
          {upcomingLoading ? (
            <div style={{ color: '#64748b' }}>Loading upcoming classes...</div>
          ) : upcoming.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
              <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#334155', marginBottom: '8px' }}>No Upcoming Confirmed Training</div>
              <p style={{ color: '#64748b', margin: 0 }}>You are not enrolled in any upcoming confirmed training classes.</p>
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '16px' }}>
              {upcoming.map((item) => (
                <div key={item.id} style={{ padding: '20px', backgroundColor: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <span style={{ fontSize: '12px', fontFamily: 'monospace', fontWeight: 'bold', color: '#64748b' }}>Batch: {item.batchId}</span>
                    <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: '#dcfce7', color: '#15803d', fontSize: '12px', fontWeight: 'bold' }}>
                      {item.status}
                    </span>
                  </div>
                  <div style={{ fontSize: '14px', color: '#475569', marginBottom: '12px' }}>
                    Enrolled on: {new Date(item.enrolledAt || item.createdAt).toLocaleDateString()}
                  </div>
                  <button
                    onClick={() => handleOpenDetail(item.id)}
                    style={{ width: '100%', padding: '8px', backgroundColor: '#2563eb', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    View Details & Schedule
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Tab 2: All Enrollments */}
      {activeTab === 'enrollments' && (
        <div>
          {enrollmentsLoading ? (
            <div style={{ color: '#64748b' }}>Loading enrollments...</div>
          ) : enrollments.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px' }}>
              <p style={{ color: '#64748b' }}>No enrollment history found.</p>
            </div>
          ) : (
            <div>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#ffffff', border: '1px solid #e2e8f0' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                    <th style={{ padding: '12px' }}>Enrollment Ref</th>
                    <th style={{ padding: '12px' }}>Batch ID</th>
                    <th style={{ padding: '12px' }}>Enrolled Date</th>
                    <th style={{ padding: '12px' }}>Status</th>
                    <th style={{ padding: '12px' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {enrollments.map((e) => (
                    <tr key={e.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                      <td style={{ padding: '12px', fontFamily: 'monospace', fontWeight: 'bold' }}>{e.id}</td>
                      <td style={{ padding: '12px' }}>{e.batchId}</td>
                      <td style={{ padding: '12px' }}>{new Date(e.enrolledAt || e.createdAt).toLocaleDateString()}</td>
                      <td style={{ padding: '12px' }}>
                        <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: e.status === 'CONFIRMED' || e.status === 'ACTIVE' ? '#dcfce7' : e.status === 'PAYMENT_PENDING' ? '#fef3c7' : '#f1f5f9', color: e.status === 'CONFIRMED' || e.status === 'ACTIVE' ? '#15803d' : e.status === 'PAYMENT_PENDING' ? '#b45309' : '#475569', fontSize: '12px', fontWeight: 'bold' }}>
                          {e.status}
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>
                        <button
                          onClick={() => handleOpenDetail(e.id)}
                          style={{ padding: '6px 12px', backgroundColor: '#f1f5f9', color: '#1e293b', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer' }}
                        >
                          Details
                        </button>
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

      {/* Tab 3: Demand Requests */}
      {activeTab === 'demands' && (
        <div>
          {demandsLoading ? (
            <div style={{ color: '#64748b' }}>Loading demand requests...</div>
          ) : demands.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px' }}>
              <p style={{ color: '#64748b' }}>You have no active demand requests for full batches.</p>
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#ffffff', border: '1px solid #e2e8f0' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                  <th style={{ padding: '12px' }}>Request ID</th>
                  <th style={{ padding: '12px' }}>Batch ID</th>
                  <th style={{ padding: '12px' }}>Requested At</th>
                  <th style={{ padding: '12px' }}>Status</th>
                  <th style={{ padding: '12px' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {demands.map((d) => (
                  <tr key={d.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '12px', fontFamily: 'monospace' }}>{d.id}</td>
                    <td style={{ padding: '12px' }}>{d.batchId}</td>
                    <td style={{ padding: '12px' }}>{new Date(d.requestedAt || d.createdAt).toLocaleString()}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: d.status === 'ACTIVE' ? '#fef3c7' : '#f1f5f9', color: d.status === 'ACTIVE' ? '#b45309' : '#475569', fontSize: '12px', fontWeight: 'bold' }}>
                        {d.status === 'ACTIVE' ? 'Waiting for Availability' : d.status}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {d.status === 'ACTIVE' && (
                        <button
                          onClick={() => handleWithdrawDemand(d.id)}
                          style={{ padding: '6px 12px', backgroundColor: '#fee2e2', color: '#991b1b', border: '1px solid #fecaca', borderRadius: '4px', cursor: 'pointer' }}
                        >
                          Withdraw
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

      {/* Enrollment Detail Modal */}
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

                <div style={{ textAlign: 'right' }}>
                  <button
                    onClick={() => setSelectedDetail(null)}
                    style={{ padding: '8px 20px', backgroundColor: '#0f172a', color: '#ffffff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
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
