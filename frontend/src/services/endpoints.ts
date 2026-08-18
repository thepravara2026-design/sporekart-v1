export const ENDPOINTS = {
  HEALTH: '/api/v1/health',
  VERSION: '/api/v1/version',

  // Auth & User Sessions
  AUTH_REGISTER: '/api/v1/auth/register',
  AUTH_LOGIN: '/api/v1/auth/login',
  AUTH_REFRESH: '/api/v1/auth/refresh',
  AUTH_LOGOUT: '/api/v1/auth/logout',
  AUTH_LOGOUT_ALL: '/api/v1/auth/logout-all',
  AUTH_ME: '/api/v1/auth/me',
  AUTH_CHANGE_PASSWORD: '/api/v1/auth/change-password',
  AUTH_SESSIONS: '/api/v1/auth/sessions',

  // Catalog
  PRODUCTS: '/api/v1/catalog/products',
  PRODUCT_BY_ID: (id: string) => `/api/v1/catalog/products/${id}`,
  CATEGORIES: '/api/v1/catalog/categories',
  CATEGORY_BY_ID: (id: string) => `/api/v1/catalog/categories/${id}`,

  // Cart & Checkout
  CART: '/api/v1/cart',
  CART_ITEMS: '/api/v1/cart/items',
  CART_ITEM_BY_ID: (itemId: string) => `/api/v1/cart/items/${itemId}`,
  CHECKOUT_PREVIEW: '/api/v1/checkout/preview',

  // Orders
  ORDERS: '/api/v1/orders',
  ORDER_BY_REFERENCE: (orderReference: string) => `/api/v1/orders/${orderReference}`,

  // Payments
  PAYMENTS: '/api/v1/payments',
  PAYMENT_VERIFY: '/api/v1/payments/verify',
  PAYMENT_BY_REFERENCE: (paymentReference: string) => `/api/v1/payments/${paymentReference}`,

  // Returns
  RETURNS_ELIGIBILITY: (orderRef: string) => `/api/v1/returns/eligibility/${orderRef}`,
  RETURNS_CREATE: '/api/v1/returns',
  RETURN_BY_REF: (returnRef: string) => `/api/v1/returns/${returnRef}`,
  RETURNS_CUSTOMER: '/api/v1/returns',
  ADMIN_RETURNS: '/api/v1/admin/returns',
  ADMIN_RETURN_APPROVE: (returnRef: string) => `/api/v1/admin/returns/${returnRef}/approve`,
  ADMIN_RETURN_REJECT: (returnRef: string) => `/api/v1/admin/returns/${returnRef}/reject`,
  ADMIN_RETURN_INSPECT: (returnRef: string) => `/api/v1/admin/returns/${returnRef}/inspect`,

  // Inventory
  INVENTORY_AVAILABILITY: (sku: string) => `/api/v1/inventory/skus/${sku}/availability`,
  INVENTORY_RESERVE: (orderId: string) => `/api/v1/inventory/reserve/${orderId}`,
  INVENTORY_RESERVATION_RELEASE: (reservationId: string) => `/api/v1/inventory/reservations/${reservationId}/release`,
  ADMIN_INVENTORY_LIST: '/api/v1/admin/inventory',
  ADMIN_INVENTORY_BY_SKU: (sku: string) => `/api/v1/admin/inventory/${sku}`,
  ADMIN_INVENTORY_MOVEMENTS: (sku: string) => `/api/v1/admin/inventory/${sku}/movements`,
  ADMIN_INVENTORY_ADJUST: (sku: string) => `/api/v1/admin/inventory/${sku}/adjustments`,
  ADMIN_INVENTORY_DAMAGED: (sku: string) => `/api/v1/admin/inventory/${sku}/damaged`,

  // Shipping
  CUSTOMER_SHIPMENT_TRACKING: (orderRef: string) => `/api/v1/orders/${orderRef}/shipment`,
  ADMIN_SHIPMENTS: '/api/v1/admin/shipments',
  ADMIN_SHIPMENT_BY_REF: (shipmentRef: string) => `/api/v1/admin/shipments/${shipmentRef}`,
  ADMIN_SHIPMENT_RETRY: (shipmentRef: string) => `/api/v1/admin/shipments/${shipmentRef}/retry`,
  ADMIN_SHIPMENT_SYNC: (shipmentRef: string) => `/api/v1/admin/shipments/${shipmentRef}/sync`,
  ADMIN_SHIPMENT_CANCEL: (shipmentRef: string) => `/api/v1/admin/shipments/${shipmentRef}/cancel`,
  ADMIN_SHIPMENT_LABEL: (shipmentRef: string) => `/api/v1/admin/shipments/${shipmentRef}/label`,
  ADMIN_SHIPMENT_MANIFEST: (shipmentRef: string) => `/api/v1/admin/shipments/${shipmentRef}/manifest`,

  // Support
  CUSTOMER_TICKETS: '/api/v1/customer/support/tickets',
  CUSTOMER_TICKET_BY_REF: (ticketRef: string) => `/api/v1/customer/support/tickets/${ticketRef}`,
  CUSTOMER_TICKET_MESSAGES: (ticketRef: string) => `/api/v1/customer/support/tickets/${ticketRef}/messages`,
  CUSTOMER_TICKET_REOPEN: (ticketRef: string) => `/api/v1/customer/support/tickets/${ticketRef}/reopen`,
  CUSTOMER_TICKET_REPLACEMENTS: (ticketRef: string) => `/api/v1/customer/support/tickets/${ticketRef}/replacements`,
  ADMIN_TICKETS: '/api/v1/admin/support/tickets',
  ADMIN_TICKET_BY_REF: (ticketRef: string) => `/api/v1/admin/support/tickets/${ticketRef}`,
  ADMIN_TICKET_MESSAGES: (ticketRef: string) => `/api/v1/admin/support/tickets/${ticketRef}/messages`,
  ADMIN_TICKET_ASSIGN: (ticketRef: string) => `/api/v1/admin/support/tickets/${ticketRef}/assign`,
  ADMIN_TICKET_PRIORITY: (ticketRef: string) => `/api/v1/admin/support/tickets/${ticketRef}/priority`,
  ADMIN_TICKET_ESCALATE: (ticketRef: string) => `/api/v1/admin/support/tickets/${ticketRef}/escalate`,
  ADMIN_TICKET_RESOLVE: (ticketRef: string) => `/api/v1/admin/support/tickets/${ticketRef}/resolve`,
  ADMIN_TICKET_CLOSE: (ticketRef: string) => `/api/v1/admin/support/tickets/${ticketRef}/close`,
  ADMIN_REPLACEMENT_APPROVE: (replacementRef: string) => `/api/v1/admin/support/replacements/${replacementRef}/approve`,

  // Reviews
  CUSTOMER_REVIEWS: '/api/v1/reviews',
  PRODUCT_REVIEWS: (productId: string) => `/api/v1/products/${productId}/reviews`,
  PRODUCT_RATING_SUMMARY: (productId: string) => `/api/v1/products/${productId}/rating-summary`,
  REVIEW_VOTE: (reviewRef: string) => `/api/v1/reviews/${reviewRef}/vote`,
  ADMIN_REVIEWS: '/api/v1/admin/reviews',
  ADMIN_REVIEW_APPROVE: (reviewRef: string) => `/api/v1/admin/reviews/${reviewRef}/approve`,
  ADMIN_REVIEW_REJECT: (reviewRef: string) => `/api/v1/admin/reviews/${reviewRef}/reject`,
  ADMIN_REVIEW_FLAG: (reviewRef: string) => `/api/v1/admin/reviews/${reviewRef}/flag`,
  ADMIN_REVIEW_REPLY: (reviewRef: string) => `/api/v1/admin/reviews/${reviewRef}/replies`,

  // Training & Batches (Admin & Trainee)
  ADMIN_TRAINING_PROGRAMS: '/api/v1/admin/training-programs',
  ADMIN_TRAINING_PROGRAM_BY_ID: (id: string) => `/api/v1/admin/training-programs/${id}`,
  ADMIN_BATCHES: '/api/v1/admin/batches',
  ADMIN_BATCH_BY_ID: (id: string) => `/api/v1/admin/batches/${id}`,
  ADMIN_ENROLLMENTS: '/api/v1/admin/enrollments',
  ADMIN_ATTENDANCE: '/api/v1/admin/attendance',
  ADMIN_TRAINING_REPORTS: '/api/v1/admin/training-reports/summary',
} as const;
