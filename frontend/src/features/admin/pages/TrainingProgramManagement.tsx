import React, { useEffect, useState } from 'react';
import {
  TrainingProgramDto,
  CreateTrainingProgramPayload,
  UpdateTrainingProgramPayload,
  fetchAdminTrainingPrograms,
  createTrainingProgram,
  updateTrainingProgram,
  activateTrainingProgram,
  deactivateTrainingProgram,
} from '../api/trainingProgramApi';

export const TrainingProgramManagement: React.FC = () => {
  const [programs, setPrograms] = useState<TrainingProgramDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [categoryFilter, setCategoryFilter] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);

  // Modal states
  const [isCreateOpen, setIsCreateOpen] = useState<boolean>(false);
  const [editingProgram, setEditingProgram] = useState<TrainingProgramDto | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  // Form states
  const [formTitle, setFormTitle] = useState<string>('');
  const [formDescription, setFormDescription] = useState<string>('');
  const [formCategory, setFormCategory] = useState<string>('GENERAL');
  const [formDurationHours, setFormDurationHours] = useState<number>(8);
  const [formPriceAmount, setFormPriceAmount] = useState<number>(999);
  const [formCurrency, setFormCurrency] = useState<string>('INR');
  const [formError, setFormError] = useState<string | null>(null);

  const loadPrograms = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAdminTrainingPrograms({
        search: search || undefined,
        status: statusFilter || undefined,
        category: categoryFilter || undefined,
        page,
        size: 10,
      });
      setPrograms(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to load training programs';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPrograms();
  }, [search, statusFilter, categoryFilter, page]);

  const handleOpenCreate = () => {
    setFormTitle('');
    setFormDescription('');
    setFormCategory('GENERAL');
    setFormDurationHours(8);
    setFormPriceAmount(999);
    setFormCurrency('INR');
    setFormError(null);
    setIsCreateOpen(true);
  };

  const handleOpenEdit = (program: TrainingProgramDto) => {
    setEditingProgram(program);
    setFormTitle(program.title);
    setFormDescription(program.description || '');
    setFormCategory(program.category);
    setFormDurationHours(program.durationHours);
    setFormPriceAmount(program.priceAmount);
    setFormCurrency(program.currency);
    setFormError(null);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formTitle.trim()) {
      setFormError('Program title is required');
      return;
    }
    try {
      const payload: CreateTrainingProgramPayload = {
        title: formTitle.trim(),
        description: formDescription.trim(),
        category: formCategory.trim(),
        durationHours: formDurationHours,
        priceAmount: formPriceAmount,
        currency: formCurrency.trim(),
      };
      await createTrainingProgram(payload);
      setIsCreateOpen(false);
      setActionSuccess('Training program created successfully');
      loadPrograms();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to create training program';
      setFormError(msg);
    }
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingProgram) return;
    try {
      const payload: UpdateTrainingProgramPayload = {
        title: formTitle.trim(),
        description: formDescription.trim(),
        category: formCategory.trim(),
        durationHours: formDurationHours,
        priceAmount: formPriceAmount,
        currency: formCurrency.trim(),
      };
      await updateTrainingProgram(editingProgram.id, payload);
      setEditingProgram(null);
      setActionSuccess('Training program updated successfully');
      loadPrograms();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to update training program';
      setFormError(msg);
    }
  };

  const handleToggleActivate = async (program: TrainingProgramDto) => {
    try {
      if (program.status === 'ACTIVE') {
        await deactivateTrainingProgram(program.id);
        setActionSuccess(`Program '${program.title}' deactivated`);
      } else {
        await activateTrainingProgram(program.id);
        setActionSuccess(`Program '${program.title}' activated`);
      }
      loadPrograms();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to change program status';
      setError(msg);
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      case 'DRAFT':
        return 'bg-amber-100 text-amber-800 border-amber-300';
      case 'INACTIVE':
        return 'bg-slate-100 text-slate-800 border-slate-300';
      case 'ARCHIVED':
        return 'bg-rose-100 text-rose-800 border-rose-300';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Training Program Management</h1>
          <p className="text-slate-500 text-sm">
            Create, update, activate, and manage reusable training program definitions
          </p>
        </div>
        <button
          id="btn-create-program"
          onClick={handleOpenCreate}
          className="inline-flex items-center justify-center px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium rounded-lg shadow transition-colors"
        >
          + Create Program
        </button>
      </div>

      {/* Alert Banners */}
      {actionSuccess && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-lg flex items-center justify-between text-sm">
          <span>{actionSuccess}</span>
          <button onClick={() => setActionSuccess(null)} className="text-emerald-500 font-bold ml-4">
            ×
          </button>
        </div>
      )}

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 text-rose-700 rounded-lg flex items-center justify-between text-sm">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="text-rose-500 font-bold ml-4">
            ×
          </button>
        </div>
      )}

      {/* Filters Bar */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">Search</label>
          <input
            id="input-search-program"
            type="text"
            placeholder="Search by title or description..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          />
        </div>
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">Status Filter</label>
          <select
            id="select-status-filter"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          >
            <option value="">All Statuses</option>
            <option value="DRAFT">DRAFT</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
            <option value="ARCHIVED">ARCHIVED</option>
          </select>
        </div>
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">Category</label>
          <input
            id="input-category-filter"
            type="text"
            placeholder="Filter by category (e.g. GENERAL)..."
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          />
        </div>
      </div>

      {/* Table Section */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-slate-500 text-sm">Loading training programs...</div>
        ) : programs.length === 0 ? (
          <div className="p-8 text-center text-slate-500 text-sm">
            No training programs found matching your criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-slate-700 uppercase font-semibold text-xs border-b border-slate-200">
                <tr>
                  <th className="px-4 py-3">Program Title / Slug</th>
                  <th className="px-4 py-3">Category</th>
                  <th className="px-4 py-3">Duration</th>
                  <th className="px-4 py-3">Price</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {programs.map((program) => (
                  <tr key={program.id} className="hover:bg-slate-50/50">
                    <td className="px-4 py-3">
                      <div className="font-semibold text-slate-900">{program.title}</div>
                      <div className="text-xs text-slate-400 font-mono">{program.slug}</div>
                    </td>
                    <td className="px-4 py-3 font-medium text-slate-700">{program.category}</td>
                    <td className="px-4 py-3">{program.durationHours} hrs</td>
                    <td className="px-4 py-3 font-semibold text-slate-800">
                      {program.currency} {program.priceAmount.toFixed(2)}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getStatusBadgeClass(
                          program.status
                        )}`}
                      >
                        {program.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right space-x-2">
                      <button
                        onClick={() => handleOpenEdit(program)}
                        className="px-2.5 py-1 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded transition-colors"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleToggleActivate(program)}
                        className={`px-2.5 py-1 text-xs font-medium rounded transition-colors ${
                          program.status === 'ACTIVE'
                            ? 'text-amber-700 bg-amber-50 hover:bg-amber-100 border border-amber-200'
                            : 'text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200'
                        }`}
                      >
                        {program.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Footer */}
        <div className="px-4 py-3 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-xs text-slate-500">
          <span>
            Page {page + 1} of {totalPages}
          </span>
          <div className="space-x-2">
            <button
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="px-3 py-1 bg-white border border-slate-300 rounded text-slate-700 disabled:opacity-50"
            >
              Previous
            </button>
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="px-3 py-1 bg-white border border-slate-300 rounded text-slate-700 disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Create Modal */}
      {isCreateOpen && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full p-6 space-y-4">
            <h2 className="text-xl font-bold text-slate-900">Create Training Program</h2>
            {formError && <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg">{formError}</div>}
            <form onSubmit={handleCreateSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Title *</label>
                <input
                  type="text"
                  required
                  value={formTitle}
                  onChange={(e) => setFormTitle(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  placeholder="e.g. Substrate Sterilization & Spawn Inoculation"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Description</label>
                <textarea
                  rows={3}
                  value={formDescription}
                  onChange={(e) => setFormDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  placeholder="Detailed course curriculum and guidelines..."
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Category</label>
                  <input
                    type="text"
                    value={formCategory}
                    onChange={(e) => setFormCategory(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Duration (Hours)</label>
                  <input
                    type="number"
                    min="0"
                    value={formDurationHours}
                    onChange={(e) => setFormDurationHours(parseInt(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Price Amount</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={formPriceAmount}
                    onChange={(e) => setFormPriceAmount(parseFloat(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Currency</label>
                  <input
                    type="text"
                    value={formCurrency}
                    onChange={(e) => setFormCurrency(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>
              <div className="flex justify-end space-x-3 pt-2">
                <button
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                  className="px-4 py-2 border border-slate-300 rounded-lg text-sm text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium rounded-lg shadow"
                >
                  Save Program
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {editingProgram && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full p-6 space-y-4">
            <h2 className="text-xl font-bold text-slate-900">Edit Training Program</h2>
            {formError && <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg">{formError}</div>}
            <form onSubmit={handleEditSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Title *</label>
                <input
                  type="text"
                  required
                  value={formTitle}
                  onChange={(e) => setFormTitle(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Description</label>
                <textarea
                  rows={3}
                  value={formDescription}
                  onChange={(e) => setFormDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Category</label>
                  <input
                    type="text"
                    value={formCategory}
                    onChange={(e) => setFormCategory(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Duration (Hours)</label>
                  <input
                    type="number"
                    min="0"
                    value={formDurationHours}
                    onChange={(e) => setFormDurationHours(parseInt(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Price Amount</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={formPriceAmount}
                    onChange={(e) => setFormPriceAmount(parseFloat(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Currency</label>
                  <input
                    type="text"
                    value={formCurrency}
                    onChange={(e) => setFormCurrency(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>
              </div>
              <div className="flex justify-end space-x-3 pt-2">
                <button
                  type="button"
                  onClick={() => setEditingProgram(null)}
                  className="px-4 py-2 border border-slate-300 rounded-lg text-sm text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium rounded-lg shadow"
                >
                  Update Program
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TrainingProgramManagement;
