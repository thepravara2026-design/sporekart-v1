import React, { useEffect, useState } from 'react';
import { returnApi, ReturnDto } from '../../../services/returnApi';

interface Props {
  returnReference: string;
  customerId?: string;
  onBack?: () => void;
}

export const ReturnDetailPage: React.FC<Props> = ({
  returnReference,
  customerId = 'cust-101',
  onBack
}) => {
  const [returnDetails, setReturnDetails] = useState<ReturnDto | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchDetails();
  }, [returnReference, customerId]);

  const fetchDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await returnApi.getReturnByReference(returnReference, customerId);
      setReturnDetails(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load return details.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="p-6 text-center text-gray-600">Loading Return #{returnReference}...</div>;
  }

  if (error || !returnDetails) {
    return <div className="p-6 text-red-600 bg-red-50 rounded-md">Error: {error || 'Return not found'}</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-lg rounded-lg space-y-6">
      <div className="flex justify-between items-center border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Return #{returnDetails.returnReference}</h1>
          <p className="text-sm text-gray-500">Associated Order: #{returnDetails.orderReference}</p>
        </div>
        <div className="text-right">
          <span className="px-3 py-1 text-sm font-semibold rounded-full bg-blue-100 text-blue-800">
            {returnDetails.status}
          </span>
          {onBack && (
            <button
              onClick={onBack}
              className="block mt-2 text-xs text-indigo-600 hover:underline"
            >
              &larr; Back to Returns
            </button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-gray-50 p-4 rounded-md">
        <div>
          <p className="text-xs text-gray-500 uppercase font-semibold">Reason Code</p>
          <p className="text-sm font-medium text-gray-800">{returnDetails.reasonCode}</p>
          <p className="text-xs text-gray-600 mt-1">{returnDetails.reasonDescription}</p>
        </div>
        <div>
          <p className="text-xs text-gray-500 uppercase font-semibold">Total Refund Amount</p>
          <p className="text-lg font-bold text-green-700">₹{returnDetails.totalRefundableAmount.toFixed(2)}</p>
          <p className="text-xs text-gray-500">Policy Version: {returnDetails.policyVersion}</p>
        </div>
      </div>

      {returnDetails.refundRecord && (
        <div className="border border-green-200 bg-green-50 p-4 rounded-md">
          <h2 className="text-md font-semibold text-green-900 mb-2">Refund Record</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-xs text-green-800">
            <div><span className="font-bold">Refund Ref:</span> {returnDetails.refundRecord.refundReference}</div>
            <div><span className="font-bold">Provider:</span> {returnDetails.refundRecord.provider}</div>
            <div><span className="font-bold">Status:</span> {returnDetails.refundRecord.status}</div>
            <div><span className="font-bold">Txn ID:</span> {returnDetails.refundRecord.providerRefundId || 'N/A'}</div>
          </div>
        </div>
      )}

      <div>
        <h2 className="text-lg font-semibold mb-3 text-gray-800">Return Items</h2>
        <div className="divide-y border rounded-md overflow-hidden">
          {returnDetails.items.map(item => (
            <div key={item.id} className="p-3.5 flex justify-between items-center text-sm">
              <div>
                <p className="font-medium text-gray-900">{item.productNameSnapshot}</p>
                <p className="text-xs text-gray-500">SKU: {item.sku} | Unit Price: ₹{item.unitPrice.toFixed(2)}</p>
              </div>
              <div className="text-right text-xs space-y-0.5">
                <p><span className="font-semibold">Requested:</span> {item.requestedQuantity}</p>
                <p><span className="font-semibold">Approved:</span> {item.approvedQuantity}</p>
                <p><span className="font-semibold">Accepted:</span> {item.acceptedQuantity}</p>
                <p className="font-semibold text-green-700">Refund: ₹{item.refundAmount.toFixed(2)}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div>
        <h2 className="text-lg font-semibold mb-3 text-gray-800">Audit Status History</h2>
        <div className="space-y-2 border-l-2 border-indigo-200 pl-4">
          {returnDetails.statusHistory.map((history, idx) => (
            <div key={idx} className="text-xs space-y-0.5">
              <span className="font-semibold text-indigo-700">{history.newStatus}</span>
              <span className="text-gray-400 ml-2">{new Date(history.timestamp).toLocaleString()}</span>
              <p className="text-gray-600">{history.reason} (by {history.actorType}: {history.actorId})</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
