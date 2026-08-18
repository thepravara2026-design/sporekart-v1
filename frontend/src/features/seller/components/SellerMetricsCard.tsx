import React from 'react';
import { Card } from '../../../components/ui/Card';

export interface SellerMetricsCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  trend?: {
    value: string;
    isPositive: boolean;
  };
  icon?: React.ReactNode;
  variant?: 'default' | 'highlight' | 'warning';
  testId?: string;
}

export const SellerMetricsCard: React.FC<SellerMetricsCardProps> = ({
  title,
  value,
  subtitle,
  trend,
  icon,
  variant = 'default',
  testId = 'seller-metrics-card',
}) => {
  const getBorderColor = () => {
    switch (variant) {
      case 'highlight':
        return 'border-forest-500 bg-forest-950/20';
      case 'warning':
        return 'border-amber-500/50 bg-amber-950/20';
      default:
        return 'border-slate-800 bg-slate-900/60';
    }
  };

  return (
    <Card
      data-testid={testId}
      className={`p-5 rounded-xl border transition-all duration-200 hover:border-slate-700 ${getBorderColor()}`}
    >
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <p className="text-xs font-medium uppercase tracking-wider text-slate-400">{title}</p>
          <p className="text-2xl font-bold tracking-tight text-slate-100">{value}</p>
        </div>
        {icon && (
          <div className="p-2.5 rounded-lg bg-slate-800/80 text-forest-400 border border-slate-700/50">
            {icon}
          </div>
        )}
      </div>

      {(subtitle || trend) && (
        <div className="mt-3 flex items-center justify-between text-xs pt-3 border-t border-slate-800/60">
          {subtitle && <span className="text-slate-400">{subtitle}</span>}
          {trend && (
            <span
              className={`font-semibold flex items-center gap-1 ${
                trend.isPositive ? 'text-emerald-400' : 'text-rose-400'
              }`}
            >
              {trend.isPositive ? '↑' : '↓'} {trend.value}
            </span>
          )}
        </div>
      )}
    </Card>
  );
};
