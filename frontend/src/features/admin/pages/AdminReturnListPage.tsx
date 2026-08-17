import React, { useEffect, useState } from 'react';
import { returnApi, ReturnDto } from '../../../services/returnApi';

export const AdminReturnListPage: React.FC = () => {
  const [returns, setReturns] = useState<ReturnDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [orderRefFilter, setOrderRefFilter] = useState<string>('');
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  useEffect(() => {
    fetchAdminReturns();
  }, [statusFilter]);

  const fetchAdminReturns = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await returnApi.listAdminReturns(undefined, orderRefFilter.trim() || undefined, statusFilter || undefined);
      setReturns(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to fetch return requests.');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (returnRef: string) => {
    setActionLoading(returnRef);
    try {
      await returnApi.approveReturn(returnRef, 'admin-1', 'Approved via admin dashboard');
      await fetchAdminReturns();
    } catch (err: unknown) {
      alert('Approval failed: ' + (err instanceof Error ? err.message : 'Error'));
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (returnRef: string) => {
    const reason = prompt('Enter rejection reason:', 'Invalid proof of damage');
    if (!reason) return;
    setActionLoading(returnRef);
    try {
      await returnApi.rejectReturn(returnRef, 'admin-1', reason);
      await fetchAdminReturns();
    } catch (err: unknown) {
      alert('Rejection failed: ' + (err instanceof Error ? err.message : 'Error'));
    } finally {
      setActionLoading(null);
    }
  };

  const handleInspect = async (returnRef: string) => {
    setActionLoading(returnRef);
    try {
      await returnApi.inspectReturn(returnRef, {
        inspectorId: 'inspector-1',
        outcome: 'ACCEPTED',
        notes: 'Passed warehouse QA'
      }, 'inspector-1');
      await fetchAdminReturns();
    } catch (err: unknown) {
      alert('Inspection processing failed: ' + (err instanceof Error ? err.message : 'Error'));
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-6 bg-white shadow-lg rounded-lg space-y-6">
      <div className="flex justify-between items-center border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Admin Returns & Refund Management</h1>
          <p className="text-sm text-gray-500">Review, approve, inspect, and process refunds for customer return requests.</p>
        </div>
        <button
          onClick={fetchAdminReturns}
          className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-medium rounded-md"
        >
          Refresh List
        </button>
      </div>

      <div className="flex flex-wrap gap-4 items-center bg-gray-50 p-4 rounded-md">
        <div>
          <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">Status Filter</label>
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            className="border border-gray-300 rounded p-1.5 text-sm"
          >
            <option value="">All Statuses</option>
            <option value="REQUESTED">REQUESTED</option>
            <option value="APPROVED">APPROVED</option>
            <option value="RECEIVED">RECEIVED</option>
            <option value="INSPECTION_PENDING">INSPECTION_PENDING</option>
            <option value="REFUND_PENDING">REFUND_PENDING</option>
            <option value="REFUNDED">REFUNDED</option>
            <option value="REJECTED">REJECTED</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">Order Ref</label>
          <div className="flex gap-2">
            <input
              type="text"
              value={orderRefFilter}
              onChange={e => setOrderRefFilter(e.target.value)}
              placeholder="e.g. ORD-10001"
              className="border border-gray-300 rounded p-1.5 text-sm"
            />
            <button
              onClick={fetchAdminReturns}
              className="px-3 py-1.5 bg-indigo-600 text-white text-xs font-medium rounded hover:bg-indigo-700"
            >
              Search
            </button>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="p-6 text-center text-gray-600">Loading returns...</div>
      ) : error ? (
        <div className="p-4 bg-red-50 text-red-700 rounded-md">Error: {error}</div>
      ) : returns.length === 0 ? (
        <div className="p-6 text-center text-gray-500 bg-gray-50 rounded-md">No return requests match the criteria.</div>
      ) : (
        <div className="overflow-x-auto border rounded-md">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Return Ref</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Order</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Customer</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Reason</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Refund Amt</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Status</th>
                <th className="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {returns.map(ret => (
                <tr key={ret.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-900">{ret.returnReference}</td>
                  <td className="px-4 py-3 text-gray-600">{ret.orderReference}</td>
                  <td className="px-4 py-3 text-gray-600">{ret.customerId}</td>
                  <td className="px-4 py-3 text-gray-600">{ret.reasonCode}</td>
                  <td className="px-4 py-3 font-semibold text-gray-900">₹{ret.totalRefundableAmount.toFixed(2)}</td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                      {ret.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center space-x-2">
                    {ret.status === 'REQUESTED' && (
                      <>
                        <button
                          onClick={() => handleApprove(ret.returnReference)}
                          disabled={actionLoading === ret.returnReference}
                          className="px-2.5 py-1 bg-green-600 hover:bg-green-700 text-white text-xs font-medium rounded disabled:opacity-50"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleReject(ret.returnReference)}
                          disabled={actionLoading === ret.returnReference}
                          className="px-2.5 py-1 bg-red-600 hover:bg-red-700 text-white text-xs font-medium rounded disabled:opacity-50"
                        >
                          Reject
                        </button>
                      </>
                    )}
                    {(ret.status === 'RECEIVED' || ret.status === 'INSPECTION_PENDING') && (
                      <button
                        onClick={() => handleInspect(ret.returnReference)}
                        disabled={actionLoading === ret.returnReference}
                        className="px-2.5 py-1 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-medium rounded disabled:opacity-50"
                      >
                        Inspect & Refund
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
