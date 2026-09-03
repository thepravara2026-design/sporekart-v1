import React from 'react';
import { Badge } from '../../../components/ui/Badge';
import { SellerPayoutRecord } from '../types/seller';

export interface PayoutSummaryTableProps {
  payouts: SellerPayoutRecord[];
  isLoading?: boolean;
  testId?: string;
}

export const PayoutSummaryTable: React.FC<PayoutSummaryTableProps> = ({
  payouts,
  isLoading = false,
  testId = 'payout-summary-table',
}) => {
  if (isLoading) {
    return (
      <div data-testid={`${testId}-loading`} className="p-8 text-center text-slate-400">
        Loading payout history...
      </div>
    );
  }

  if (!payouts || payouts.length === 0) {
    return (
      <div data-testid={`${testId}-empty`} className="p-8 text-center text-slate-400">
        No payout records available.
      </div>
    );
  }

  return (
    <div data-testid={testId} className="overflow-x-auto rounded-lg border border-slate-800 bg-slate-900/40">
      <table className="w-full text-left text-sm text-slate-200">
        <thead className="bg-slate-800/80 text-xs uppercase tracking-wider text-slate-400 border-b border-slate-800">
          <tr>
            <th className="px-4 py-3 font-semibold">Reference</th>
            <th className="px-4 py-3 font-semibold">Period</th>
            <th className="px-4 py-3 font-semibold">Amount</th>
            <th className="px-4 py-3 font-semibold">Status</th>
            <th className="px-4 py-3 font-semibold">Bank Account</th>
            <th className="px-4 py-3 font-semibold">Date</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800/60">
          {payouts.map((payout) => (
            <tr key={payout.id} className="hover:bg-slate-800/30 transition-colors">
              <td className="px-4 py-3 font-mono font-medium text-forest-400">{payout.payoutReference}</td>
              <td className="px-4 py-3 text-slate-300">{payout.period}</td>
              <td className="px-4 py-3 font-semibold text-slate-100">
                ₹{payout.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </td>
              <td className="px-4 py-3">
                <Badge
                  variant={
                    payout.status === 'COMPLETED'
                      ? 'success'
                      : payout.status === 'PENDING'
                      ? 'warning'
                      : 'info'
                  }
                >
                  {payout.status}
                </Badge>
              </td>
              <td className="px-4 py-3 font-mono text-xs text-slate-400">•••• {payout.bankAccountLast4}</td>
              <td className="px-4 py-3 text-xs text-slate-400">
                {new Date(payout.payoutDate).toLocaleDateString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
