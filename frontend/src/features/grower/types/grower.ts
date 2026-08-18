export interface GrowerProfile {
  id: string;
  email: string;
  businessName: string;
  contactName: string;
  phone?: string;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  status: 'ACTIVE' | 'PENDING_VERIFICATION' | 'SUSPENDED';
  role: string;
  joinedAt: string;
  bio?: string;
  certificationStatus?: string;
}

export interface GrowerDashboardMetrics {
  activeProductsCount: number;
  totalInventoryOnHand: number;
  lowStockItemsCount: number;
  outOfStockItemsCount: number;
  pendingOrdersCount: number;
  activeShipmentsCount: number;
  pendingReturnsCount: number;
  totalRevenue: number;
  monthlySalesVolume: number;
}

export interface GrowerOperationalStatus {
  facilityStatus: 'OPERATIONAL' | 'MAINTENANCE' | 'LIMITED_CAPACITY';
  fulfillmentCapacityPercent: number;
  lastInventoryAuditDate: string;
  activeAlertsCount: number;
}

export interface GrowerSettings {
  emailNotifications: boolean;
  lowStockAlertThreshold: number;
  autoAcknowledgeOrders: boolean;
  preferredCarrier: string;
  defaultFulfillmentLocation: string;
  currency: string;
}

export interface GrowerReportSummary {
  period: string;
  totalSales: number;
  totalOrders: number;
  unitsSold: number;
  fulfillmentRate: number;
  averageOrderValue: number;
  returnRatePercent: number;
}
