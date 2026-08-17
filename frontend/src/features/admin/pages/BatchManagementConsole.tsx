import React, { useEffect, useState } from 'react';
import {
  BatchDto,
  CreateBatchPayload,
  UpdateBatchPayload,
  fetchAdminBatches,
  createBatch,
  updateBatch,
  activateBatch,
  deactivateBatch,
  cancelBatch,
} from '../api/batchApi';

export const BatchManagementConsole: React.FC = () => {
  const [batches, setBatches] = useState<BatchDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [programIdFilter, setProgramIdFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [deliveryModeFilter, setDeliveryModeFilter] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);

  // Modal states
  const [isCreateOpen, setIsCreateOpen] = useState<boolean>(false);
  const [editingBatch, setEditingBatch] = useState<BatchDto | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  // Form states
  const [formProgramId, setFormProgramId] = useState<string>('');
  const [formBatchCode, setFormBatchCode] = useState<string>('');
  const [formStartDate, setFormStartDate] = useState<string>('');
  const [formEndDate, setFormEndDate] = useState<string>('');
  const [formTotalCapacity, setFormTotalCapacity] = useState<number>(20);
  const [formDeliveryMode, setFormDeliveryMode] = useState<'ONLINE' | 'OFFLINE' | 'HYBRID'>('ONLINE');
  const [formVenueInfo, setFormVenueInfo] = useState<string>('');
  const [formMeetingUrl, setFormMeetingUrl] = useState<string>('');
  const [formTimezone, setFormTimezone] = useState<string>('Asia/Kolkata');
  const [formError, setFormError] = useState<string | null>(null);

  const loadBatches = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAdminBatches({
        programId: programIdFilter || undefined,
        status: statusFilter || undefined,
        deliveryMode: deliveryModeFilter || undefined,
        page,
        size: 10,
      });
      setBatches(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load training batches');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBatches();
  }, [programIdFilter, statusFilter, deliveryModeFilter, page]);

  const handleOpenCreate = () => {
    setFormProgramId('');
    setFormBatchCode(`TRN-${new Date().getFullYear()}-${Math.floor(100 + Math.random() * 900)}`);
    const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 16);
    const nextWeek = new Date(Date.now() + 7 * 86400000).toISOString().slice(0, 16);
    setFormStartDate(tomorrow);
    setFormEndDate(nextWeek);
    setFormTotalCapacity(20);
    setFormDeliveryMode('ONLINE');
    setFormVenueInfo('');
    setFormMeetingUrl('https://meet.google.com/abc-defg-hij');
    setFormTimezone('Asia/Kolkata');
    setFormError(null);
    setIsCreateOpen(true);
  };

  const handleOpenEdit = (batch: BatchDto) => {
    setEditingBatch(batch);
    setFormProgramId(batch.programId);
    setFormStartDate(new Date(batch.startDate).toISOString().slice(0, 16));
    setFormEndDate(new Date(batch.endDate).toISOString().slice(0, 16));
    setFormDeliveryMode(batch.deliveryMode);
    setFormVenueInfo(batch.venueInfo || '');
    setFormMeetingUrl(batch.meetingUrl || '');
    setFormTimezone(batch.timezone);
    setFormError(null);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formProgramId.trim()) {
      setFormError('Program ID is required');
      return;
    }
    if (!formBatchCode.trim()) {
      setFormError('Batch code is required');
      return;
    }
    if (!formStartDate || !formEndDate) {
      setFormError('Start date and end date are required');
      return;
    }
    if (new Date(formStartDate) >= new Date(formEndDate)) {
      setFormError('Start date must be before end date');
      return;
    }

    try {
      const payload: CreateBatchPayload = {
        programId: formProgramId.trim(),
        batchCode: formBatchCode.trim(),
        startDate: new Date(formStartDate).toISOString(),
        endDate: new Date(formEndDate).toISOString(),
        totalCapacity: formTotalCapacity,
        deliveryMode: formDeliveryMode,
        venueInfo: formVenueInfo.trim(),
        meetingUrl: formMeetingUrl.trim(),
        timezone: formTimezone.trim(),
      };
      await createBatch(payload);
      setIsCreateOpen(false);
      setActionSuccess('Training batch created successfully');
      loadBatches();
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to create training batch');
    }
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingBatch) return;

    if (formStartDate && formEndDate && new Date(formStartDate) >= new Date(formEndDate)) {
      setFormError('Start date must be before end date');
      return;
    }

    try {
      const payload: UpdateBatchPayload = {
        startDate: formStartDate ? new Date(formStartDate).toISOString() : undefined,
        endDate: formEndDate ? new Date(formEndDate).toISOString() : undefined,
        timezone: formTimezone.trim(),
        deliveryMode: formDeliveryMode,
        venueInfo: formVenueInfo.trim(),
        meetingUrl: formMeetingUrl.trim(),
      };
      await updateBatch(editingBatch.id, payload);
      setEditingBatch(null);
      setActionSuccess('Training batch updated successfully');
      loadBatches();
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to update training batch');
    }
  };

  const handleToggleActivate = async (batch: BatchDto) => {
    try {
      if (batch.status === 'ACTIVE' || batch.status === 'FULL') {
        await deactivateBatch(batch.id);
        setActionSuccess(`Batch ${batch.batchCode} deactivated`);
      } else {
        await activateBatch(batch.id);
        setActionSuccess(`Batch ${batch.batchCode} activated`);
      }
      loadBatches();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to toggle batch status');
    }
  };

  const handleCancel = async (batch: BatchDto) => {
    if (!window.confirm(`Are you sure you want to cancel batch ${batch.batchCode}?`)) return;
    try {
      await cancelBatch(batch.id);
      setActionSuccess(`Batch ${batch.batchCode} cancelled`);
      loadBatches();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to cancel batch');
    }
  };

  const getStatusBadgeStyle = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30';
      case 'PLANNED':
        return 'bg-sky-500/20 text-sky-400 border-sky-500/30';
      case 'SCHEDULED':
        return 'bg-indigo-500/20 text-indigo-400 border-indigo-500/30';
      case 'FULL':
        return 'bg-amber-500/20 text-amber-400 border-amber-500/30';
      case 'COMPLETED':
        return 'bg-slate-500/20 text-slate-400 border-slate-500/30';
      case 'CANCELLED':
        return 'bg-rose-500/20 text-rose-400 border-rose-500/30';
      default:
        return 'bg-zinc-500/20 text-zinc-400 border-zinc-500/30';
    }
  };

  return (
    <div className="p-6 bg-zinc-950 min-h-screen text-zinc-100">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-zinc-800 pb-5">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-white">Batch & Schedule Management</h1>
            <p className="text-sm text-zinc-400">Schedule concrete occurrences of Training Programs and manage venue/delivery options</p>
          </div>
          <button
            onClick={handleOpenCreate}
            className="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white transition-colors shadow-lg shadow-emerald-950/40"
          >
            + Schedule New Batch
          </button>
        </div>

        {/* Action Success Alert */}
        {actionSuccess && (
          <div className="p-4 rounded-lg bg-emerald-950/50 border border-emerald-800/60 text-emerald-300 text-sm flex items-center justify-between">
            <span>{actionSuccess}</span>
            <button onClick={() => setActionSuccess(null)} className="text-emerald-400 hover:text-emerald-200">
              ✕
            </button>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div className="p-4 rounded-lg bg-rose-950/50 border border-rose-800/60 text-rose-300 text-sm flex items-center justify-between">
            <span>{error}</span>
            <button onClick={() => setError(null)} className="text-rose-400 hover:text-rose-200">
              ✕
            </button>
          </div>
        )}

        {/* Filters */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 bg-zinc-900/60 p-4 rounded-xl border border-zinc-800">
          <div>
            <label className="block text-xs font-semibold text-zinc-400 mb-1 uppercase tracking-wider">Program ID</label>
            <input
              type="text"
              value={programIdFilter}
              onChange={(e) => setProgramIdFilter(e.target.value)}
              placeholder="Filter by Program ID..."
              className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-zinc-400 mb-1 uppercase tracking-wider">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-200 focus:outline-none focus:border-emerald-500"
            >
              <option value="">All Statuses</option>
              <option value="PLANNED">PLANNED</option>
              <option value="SCHEDULED">SCHEDULED</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="FULL">FULL</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-zinc-400 mb-1 uppercase tracking-wider">Delivery Mode</label>
            <select
              value={deliveryModeFilter}
              onChange={(e) => setDeliveryModeFilter(e.target.value)}
              className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-200 focus:outline-none focus:border-emerald-500"
            >
              <option value="">All Delivery Modes</option>
              <option value="ONLINE">ONLINE</option>
              <option value="OFFLINE">OFFLINE</option>
              <option value="HYBRID">HYBRID</option>
            </select>
          </div>
        </div>

        {/* Data Table */}
        <div className="bg-zinc-900/40 rounded-xl border border-zinc-800 overflow-hidden">
          {loading ? (
            <div className="p-12 text-center text-zinc-400">Loading training batches...</div>
          ) : batches.length === 0 ? (
            <div className="p-12 text-center text-zinc-400">No training batches found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-zinc-300">
                <thead className="bg-zinc-900 text-xs uppercase tracking-wider text-zinc-400 border-b border-zinc-800">
                  <tr>
                    <th className="px-6 py-4">Batch Code</th>
                    <th className="px-6 py-4">Program ID</th>
                    <th className="px-6 py-4">Schedule (Dates & TZ)</th>
                    <th className="px-6 py-4">Mode / Venue</th>
                    <th className="px-6 py-4">Capacity</th>
                    <th className="px-6 py-4">Status</th>
                    <th className="px-6 py-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-800/60">
                  {batches.map((batch) => (
                    <tr key={batch.id} className="hover:bg-zinc-900/50 transition-colors">
                      <td className="px-6 py-4 font-mono font-medium text-emerald-400">{batch.batchCode}</td>
                      <td className="px-6 py-4 font-mono text-xs text-zinc-400">{batch.programId}</td>
                      <td className="px-6 py-4 text-xs space-y-1">
                        <div><span className="text-zinc-500">Start:</span> {new Date(batch.startDate).toLocaleString()}</div>
                        <div><span className="text-zinc-500">End:</span> {new Date(batch.endDate).toLocaleString()}</div>
                        <div className="text-zinc-500 text-[11px]">TZ: {batch.timezone}</div>
                      </td>
                      <td className="px-6 py-4 text-xs">
                        <div className="font-semibold text-zinc-200">{batch.deliveryMode}</div>
                        {batch.venueInfo && <div className="text-zinc-400 truncate max-w-[160px]">{batch.venueInfo}</div>}
                        {batch.meetingUrl && <div className="text-sky-400 truncate max-w-[160px]">{batch.meetingUrl}</div>}
                      </td>
                      <td className="px-6 py-4 text-xs font-mono">
                        {batch.occupiedSeats} / {batch.totalCapacity}
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getStatusBadgeStyle(batch.status)}`}>
                          {batch.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right space-x-2">
                        <button
                          onClick={() => handleOpenEdit(batch)}
                          className="px-3 py-1 text-xs rounded bg-zinc-800 hover:bg-zinc-700 text-zinc-200 border border-zinc-700"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleToggleActivate(batch)}
                          className="px-3 py-1 text-xs rounded bg-amber-950/60 hover:bg-amber-900/60 text-amber-300 border border-amber-800/60"
                        >
                          {batch.status === 'ACTIVE' || batch.status === 'FULL' ? 'Deactivate' : 'Activate'}
                        </button>
                        {batch.status !== 'CANCELLED' && batch.status !== 'COMPLETED' && (
                          <button
                            onClick={() => handleCancel(batch)}
                            className="px-3 py-1 text-xs rounded bg-rose-950/60 hover:bg-rose-900/60 text-rose-300 border border-rose-800/60"
                          >
                            Cancel
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between px-6 py-4 bg-zinc-900 border-t border-zinc-800 text-xs text-zinc-400">
              <div>Page {page + 1} of {totalPages}</div>
              <div className="space-x-2">
                <button
                  disabled={page === 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  className="px-3 py-1 rounded bg-zinc-800 hover:bg-zinc-700 disabled:opacity-50 text-zinc-300"
                >
                  Previous
                </button>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                  className="px-3 py-1 rounded bg-zinc-800 hover:bg-zinc-700 disabled:opacity-50 text-zinc-300"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Modal: Create Batch */}
      {isCreateOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-zinc-900 border border-zinc-800 rounded-2xl max-w-lg w-full p-6 space-y-5 shadow-2xl">
            <h2 className="text-xl font-bold text-white">Schedule New Training Batch</h2>

            {formError && (
              <div className="p-3 rounded-lg bg-rose-950/60 border border-rose-800 text-rose-300 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleCreateSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block text-zinc-400 mb-1">Program ID *</label>
                <input
                  type="text"
                  required
                  value={formProgramId}
                  onChange={(e) => setFormProgramId(e.target.value)}
                  placeholder="e.g. prog-uuid-123"
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-zinc-400 mb-1">Batch Code *</label>
                  <input
                    type="text"
                    required
                    value={formBatchCode}
                    onChange={(e) => setFormBatchCode(e.target.value)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500 font-mono"
                  />
                </div>
                <div>
                  <label className="block text-zinc-400 mb-1">Total Capacity *</label>
                  <input
                    type="number"
                    min={1}
                    value={formTotalCapacity}
                    onChange={(e) => setFormTotalCapacity(parseInt(e.target.value) || 1)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-zinc-400 mb-1">Start Date & Time *</label>
                  <input
                    type="datetime-local"
                    required
                    value={formStartDate}
                    onChange={(e) => setFormStartDate(e.target.value)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-zinc-400 mb-1">End Date & Time *</label>
                  <input
                    type="datetime-local"
                    required
                    value={formEndDate}
                    onChange={(e) => setFormEndDate(e.target.value)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-zinc-400 mb-1">Delivery Mode</label>
                  <select
                    value={formDeliveryMode}
                    onChange={(e) => setFormDeliveryMode(e.target.value as any)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ONLINE">ONLINE</option>
                    <option value="OFFLINE">OFFLINE</option>
                    <option value="HYBRID">HYBRID</option>
                  </select>
                </div>
                <div>
                  <label className="block text-zinc-400 mb-1">Timezone</label>
                  <input
                    type="text"
                    value={formTimezone}
                    onChange={(e) => setFormTimezone(e.target.value)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-zinc-400 mb-1">Venue Info (Offline / Hybrid)</label>
                <input
                  type="text"
                  value={formVenueInfo}
                  onChange={(e) => setFormVenueInfo(e.target.value)}
                  placeholder="Building, Lab Room, City..."
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-zinc-400 mb-1">Meeting URL (Online / Hybrid)</label>
                <input
                  type="url"
                  value={formMeetingUrl}
                  onChange={(e) => setFormMeetingUrl(e.target.value)}
                  placeholder="https://..."
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                  className="px-4 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-medium shadow-md"
                >
                  Schedule Batch
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Edit Batch */}
      {editingBatch && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-zinc-900 border border-zinc-800 rounded-2xl max-w-lg w-full p-6 space-y-5 shadow-2xl">
            <h2 className="text-xl font-bold text-white">Edit Schedule — Batch {editingBatch.batchCode}</h2>

            {formError && (
              <div className="p-3 rounded-lg bg-rose-950/60 border border-rose-800 text-rose-300 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleEditSubmit} className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-zinc-400 mb-1">Start Date & Time</label>
                  <input
                    type="datetime-local"
                    value={formStartDate}
                    onChange={(e) => setFormStartDate(e.target.value)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-zinc-400 mb-1">End Date & Time</label>
                  <input
                    type="datetime-local"
                    value={formEndDate}
                    onChange={(e) => setFormEndDate(e.target.value)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-zinc-400 mb-1">Delivery Mode</label>
                  <select
                    value={formDeliveryMode}
                    onChange={(e) => setFormDeliveryMode(e.target.value as any)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ONLINE">ONLINE</option>
                    <option value="OFFLINE">OFFLINE</option>
                    <option value="HYBRID">HYBRID</option>
                  </select>
                </div>
                <div>
                  <label className="block text-zinc-400 mb-1">Timezone</label>
                  <input
                    type="text"
                    value={formTimezone}
                    onChange={(e) => setFormTimezone(e.target.value)}
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-zinc-400 mb-1">Venue Info</label>
                <input
                  type="text"
                  value={formVenueInfo}
                  onChange={(e) => setFormVenueInfo(e.target.value)}
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-zinc-400 mb-1">Meeting URL</label>
                <input
                  type="url"
                  value={formMeetingUrl}
                  onChange={(e) => setFormMeetingUrl(e.target.value)}
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-2 text-zinc-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setEditingBatch(null)}
                  className="px-4 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-medium shadow-md"
                >
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
