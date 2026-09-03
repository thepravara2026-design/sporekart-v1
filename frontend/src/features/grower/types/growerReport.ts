export interface SalesMetricPoint {
  date: string;
  revenue: number;
  ordersCount: number;
}

export interface TopProductPerformance {
  sku: string;
  productName: string;
  unitsSold: number;
  totalRevenue: number;
}

export interface GrowerAnalyticsReport {
  generatedAt: string;
  periodLabel: string;
  totalSalesRevenue: number;
  ordersFulfilled: number;
  inventoryTurnoverRate: number;
  fulfillmentAccuracyPercent: number;
  salesTrend: SalesMetricPoint[];
  topProducts: TopProductPerformance[];
}
