export interface NavItem {
  label: string;
  path: string;
  icon?: string;
  badge?: string | number;
  roles?: string[];
  external?: boolean;
}

export interface FooterNavGroup {
  heading: string;
  links: Array<{ label: string; path: string; external?: boolean }>;
}

/** Public-facing primary navigation (Header + MobileNav) */
export const MAIN_NAVIGATION: NavItem[] = [
  { label: 'Home', path: '/' },
  { label: 'Products', path: '/products' },
  { label: 'Categories', path: '/categories' },
];

/** Admin dashboard navigation */
export const ADMIN_NAVIGATION: NavItem[] = [
  { label: 'Dashboard', path: '/admin' },
  { label: 'Training Programs', path: '/admin/training-programs' },
  { label: 'Batch Management', path: '/admin/batches' },
  { label: 'Notification Operations', path: '/admin/notifications' },
  { label: 'Training Reporting', path: '/admin/reporting' },
];

/** Authenticated account navigation */
export const ACCOUNT_NAVIGATION: NavItem[] = [
  { label: 'My Account', path: '/account' },
  { label: 'Orders & Shipments', path: '/account/orders' },
  { label: 'Training Enrollments', path: '/account/trainings' },
  { label: 'Support Tickets', path: '/account/support' },
];

/** Footer navigation columns */
export const FOOTER_NAVIGATION: FooterNavGroup[] = [
  {
    heading: 'Catalog',
    links: [
      { label: 'All Products', path: '/products' },
      { label: 'All Categories', path: '/categories' },
    ],
  },
  {
    heading: 'Support',
    links: [
      { label: 'System Health', path: '/health' },
    ],
  },
];
