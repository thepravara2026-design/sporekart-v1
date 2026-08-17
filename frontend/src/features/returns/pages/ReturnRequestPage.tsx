import React, { useEffect, useState } from 'react';
import { returnApi, ReturnEligibilityDto } from '../../../services/returnApi';

interface Props {
  orderReference: string;
  customerId?: string;
  onReturnSubmitted?: (returnRef: string) => void;
}

export const ReturnRequestPage: React.FC<Props> = ({
  orderReference,
  customerId = 'cust-101',
  onReturnSubmitted
}) => {
  const [eligibility, setEligibility] = useState<ReturnEligibilityDto | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [reasonCode, setReasonCode] = useState<string>('DAMAGED');
  const [reasonDescription, setReasonDescription] = useState<string>('');
  const [evidenceUrls, setEvidenceUrls] = useState<string>('');
  const [selectedQuantities, setSelectedQuantities] = useState<Record<string, number>>({});
  const [submitting, setSubmitting] = useState<boolean>(false);

  useEffect(() => {
    fetchEligibility();
  }, [orderReference, customerId]);

  const fetchEligibility = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await returnApi.checkEligibility(orderReference, customerId);
      setEligibility(data);
      const initialQty: Record<string, number> = {};
      data.items.forEach(item => {
        if (item.returnable) {
          initialQty[item.orderItemId] = item.returnableQuantity;
        }
      });
      setSelectedQuantities(initialQty);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to evaluate return eligibility.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuantityChange = (orderItemId: string, qty: number) => {
    setSelectedQuantities(prev => ({
      ...prev,
      [orderItemId]: Math.max(0, qty)
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!eligibility || !eligibility.eligible) return;

    const returnItems = Object.entries(selectedQuantities)
      .filter(([, qty]) => qty > 0)
      .map(([orderItemId, qty]) => ({
        orderItemId,
        quantity: qty
      }));

    if (returnItems.length === 0) {
      setError('Please select at least one item to return.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const result = await returnApi.createReturn(orderReference, {
        reasonCode,
        reasonDescription,
        evidenceUrls: evidenceUrls.trim() || undefined,
        items: returnItems
      }, customerId);

      if (onReturnSubmitted) {
        onReturnSubmitted(result.returnReference);
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to submit return request.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="p-6 text-center text-gray-600">Evaluating return eligibility for Order #{orderReference}...</div>;
  }

  if (error && !eligibility) {
    return <div className="p-6 text-red-600 bg-red-50 rounded-md">Error: {error}</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-lg rounded-lg">
      <h1 className="text-2xl font-bold mb-4 text-gray-800">Request Return for Order #{orderReference}</h1>

      {eligibility && !eligibility.eligible ? (
        <div className="p-4 mb-6 bg-amber-50 border border-amber-200 text-amber-800 rounded-md">
          <p className="font-semibold">This order is not eligible for return.</p>
          <p className="text-sm mt-1">Reason: {eligibility.ineligibilityReason}</p>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && <div className="p-3 bg-red-50 text-red-700 rounded text-sm">{error}</div>}

          <div>
            <label className="block font-medium text-gray-700 mb-1">Return Reason</label>
            <select
              value={reasonCode}
              onChange={e => setReasonCode(e.target.value)}
              className="w-full border-gray-300 rounded-md p-2 border shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
            >
              <option value="DAMAGED">Damaged on arrival</option>
              <option value="DEFECTIVE">Defective / Malfunctioning</option>
              <option value="WRONG_ITEM">Wrong item received</option>
              <option value="EXPIRED">Expired product</option>
              <option value="NOT_AS_DESCRIBED">Item not as described</option>
              <option value="CHANGED_MIND">Changed mind (unopened)</option>
            </select>
          </div>

          <div>
            <label className="block font-medium text-gray-700 mb-1">Description / Notes</label>
            <textarea
              rows={3}
              value={reasonDescription}
              onChange={e => setReasonDescription(e.target.value)}
              placeholder="Provide specific details regarding why you are returning these items..."
              className="w-full border-gray-300 rounded-md p-2 border shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>

          <div>
            <label className="block font-medium text-gray-700 mb-1">Photo / Video Evidence URLs (optional)</label>
            <input
              type="text"
              value={evidenceUrls}
              onChange={e => setEvidenceUrls(e.target.value)}
              placeholder="https://example.com/photo1.jpg"
              className="w-full border-gray-300 rounded-md p-2 border shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          <div>
            <h2 className="text-lg font-semibold mb-2 text-gray-800">Select Items to Return</h2>
            <div className="divide-y border rounded-md">
              {eligibility?.items.map(item => (
                <div key={item.orderItemId} className="p-4 flex items-center justify-between">
                  <div>
                    <p className="font-medium text-gray-900">{item.productNameSnapshot}</p>
                    <p className="text-xs text-gray-500">SKU: {item.sku} | Price: ₹{item.unitPrice.toFixed(2)}</p>
                    <p className="text-xs text-gray-600 mt-1">
                      Max returnable: {item.returnableQuantity} of {item.originalQuantity}
                    </p>
                  </div>
                  {item.returnable ? (
                    <div className="flex items-center space-x-2">
                      <label className="text-sm text-gray-600">Qty:</label>
                      <input
                        type="number"
                        min="0"
                        max={item.returnableQuantity}
                        value={selectedQuantities[item.orderItemId] || 0}
                        onChange={e => handleQuantityChange(item.orderItemId, parseInt(e.target.value) || 0)}
                        className="w-20 border border-gray-300 rounded p-1 text-center"
                      />
                    </div>
                  ) : (
                    <span className="text-xs text-red-500 bg-red-50 p-1.5 rounded">{item.ineligibilityReason}</span>
                  )}
                </div>
              ))}
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 px-4 rounded-md shadow-sm transition disabled:opacity-50"
          >
            {submitting ? 'Submitting Return Request...' : 'Submit Return Request'}
          </button>
        </form>
      )}
    </div>
  );
};
