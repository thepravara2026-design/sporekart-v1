export interface NavItem {
  label: string;
  path: string;
  icon?: string;
  badge?: string | number;
  roles?: string[];
  external?: boolean;
}

export const MAIN_NAVIGATION: NavItem[] = [
  { label: 'Home', path: '/' },
  { label: 'Products', path: '/products' },
  { label: 'Categories', path: '/categories' },
  { label: 'System Health', path: '/health' },
  { label: 'Design Tokens', path: '/design-system-showcase' },
];

export const ADMIN_NAVIGATION: NavItem[] = [
  { label: 'Dashboard', path: '/admin' },
  { label: 'Training Programs', path: '/admin/training-programs' },
  { label: 'Batch Management', path: '/admin/batches' },
  { label: 'Notification Operations', path: '/admin/notifications' },
  { label: 'Training Reporting', path: '/admin/reporting' },
];

export const ACCOUNT_NAVIGATION: NavItem[] = [
  { label: 'My Account', path: '/account' },
  { label: 'Orders & Shipments', path: '/account/orders' },
  { label: 'Training Enrollments', path: '/account/trainings' },
  { label: 'Support Tickets', path: '/account/support' },
];
