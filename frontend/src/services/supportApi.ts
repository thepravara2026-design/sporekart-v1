import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';

export interface SupportMessageDto {
  id: string;
  ticketId: string;
  authorId: string;
  authorType: 'CUSTOMER' | 'AGENT' | 'SYSTEM';
  visibility: 'CUSTOMER_VISIBLE' | 'INTERNAL_NOTE';
  content: string;
  attachmentUrls?: string;
  createdAt: string;
}

export interface ReplacementRequestDto {
  id: string;
  replacementReference: string;
  ticketId: string;
  ticketNumber: string;
  orderId: string;
  orderReference: string;
  customerId: string;
  orderItemId: string;
  productId: string;
  sku: string;
  quantity: number;
  reason: string;
  status: 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'INVENTORY_RESERVED' | 'SHIPMENT_CREATED' | 'DELIVERED' | 'CANCELLED' | 'FAILED';
  reservationId?: string;
  replacementShipmentId?: string;
  adminNotes?: string;
  approvedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SupportTicketDto {
  id: string;
  ticketNumber: string;
  customerId: string;
  orderId?: string;
  orderReference?: string;
  category: 'ORDER' | 'PAYMENT' | 'DELIVERY' | 'RETURN' | 'REFUND' | 'PRODUCT' | 'REPLACEMENT' | 'ACCOUNT' | 'OTHER';
  issueType: string;
  status: 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'WAITING_FOR_CUSTOMER' | 'WAITING_FOR_INTERNAL' | 'ESCALATED' | 'RESOLVED' | 'CLOSED' | 'REOPENED' | 'CANCELLED';
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  source: 'CUSTOMER_WEB' | 'CUSTOMER_APP' | 'ADMIN' | 'SYSTEM' | 'EMAIL';
  subject: string;
  description: string;
  assignedAgentId?: string;
  firstResponseAt?: string;
  firstResponseDueAt: string;
  resolutionDueAt: string;
  slaStatus: 'MET' | 'AT_RISK' | 'BREACHED';
  resolvedAt?: string;
  closedAt?: string;
  createdAt: string;
  updatedAt: string;
  messages: SupportMessageDto[];
  replacementRequests: ReplacementRequestDto[];
}

export const supportApi = {
  createTicket: async (
    payload: {
      orderId?: string;
      orderReference?: string;
      category: string;
      issueType: string;
      priority?: string;
      source?: string;
      subject: string;
      description: string;
      attachmentUrls?: string;
    },
    customerId: string
  ): Promise<SupportTicketDto> => {
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.CUSTOMER_TICKETS,
      payload,
      { headers: { 'X-Customer-Id': customerId } }
    );
    return response.data;
  },

  listCustomerTickets: async (customerId: string): Promise<SupportTicketDto[]> => {
    const response = await axiosInstance.get<SupportTicketDto[]>(
      ENDPOINTS.CUSTOMER_TICKETS,
      { headers: { 'X-Customer-Id': customerId } }
    );
    return response.data;
  },

  getCustomerTicket: async (ticketRef: string, customerId: string): Promise<SupportTicketDto> => {
    const response = await axiosInstance.get<SupportTicketDto>(
      ENDPOINTS.CUSTOMER_TICKET_BY_REF(ticketRef),
      { headers: { 'X-Customer-Id': customerId } }
    );
    return response.data;
  },

  addCustomerMessage: async (
    ticketRef: string,
    content: string,
    attachmentUrls?: string,
    customerId?: string
  ): Promise<SupportTicketDto> => {
    const headers = customerId ? { 'X-Customer-Id': customerId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.CUSTOMER_TICKET_MESSAGES(ticketRef),
      { content, attachmentUrls },
      { headers }
    );
    return response.data;
  },

  reopenTicket: async (ticketRef: string, reason?: string, customerId?: string): Promise<SupportTicketDto> => {
    const headers = customerId ? { 'X-Customer-Id': customerId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.CUSTOMER_TICKET_REOPEN(ticketRef),
      { reason },
      { headers }
    );
    return response.data;
  },

  requestReplacement: async (
    ticketRef: string,
    replacementPayload: {
      orderId: string;
      orderReference: string;
      orderItemId: string;
      productId: string;
      sku: string;
      quantity: number;
      reason: string;
    },
    customerId: string
  ): Promise<ReplacementRequestDto> => {
    const response = await axiosInstance.post<ReplacementRequestDto>(
      ENDPOINTS.CUSTOMER_TICKET_REPLACEMENTS(ticketRef),
      replacementPayload,
      { headers: { 'X-Customer-Id': customerId } }
    );
    return response.data;
  },

  listAdminTickets: async (filter?: {
    status?: string;
    priority?: string;
    category?: string;
    assignedAgentId?: string;
    searchKey?: string;
  }): Promise<SupportTicketDto[]> => {
    const response = await axiosInstance.get<SupportTicketDto[]>(
      ENDPOINTS.ADMIN_TICKETS,
      { params: filter }
    );
    return response.data;
  },

  getAdminTicket: async (ticketRef: string): Promise<SupportTicketDto> => {
    const response = await axiosInstance.get<SupportTicketDto>(
      ENDPOINTS.ADMIN_TICKET_BY_REF(ticketRef)
    );
    return response.data;
  },

  addAgentMessage: async (
    ticketRef: string,
    content: string,
    visibility: 'CUSTOMER_VISIBLE' | 'INTERNAL_NOTE' = 'CUSTOMER_VISIBLE',
    adminId?: string
  ): Promise<SupportTicketDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.ADMIN_TICKET_MESSAGES(ticketRef),
      { content, visibility },
      { headers }
    );
    return response.data;
  },

  assignTicket: async (ticketRef: string, agentId: string, adminId?: string): Promise<SupportTicketDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.ADMIN_TICKET_ASSIGN(ticketRef),
      { agentId },
      { headers }
    );
    return response.data;
  },

  updatePriority: async (ticketRef: string, priority: string, adminId?: string): Promise<SupportTicketDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.ADMIN_TICKET_PRIORITY(ticketRef),
      { priority },
      { headers }
    );
    return response.data;
  },

  escalateTicket: async (ticketRef: string, reason?: string, adminId?: string): Promise<SupportTicketDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.ADMIN_TICKET_ESCALATE(ticketRef),
      { reason },
      { headers }
    );
    return response.data;
  },

  resolveTicket: async (ticketRef: string, notes?: string, adminId?: string): Promise<SupportTicketDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.ADMIN_TICKET_RESOLVE(ticketRef),
      { notes },
      { headers }
    );
    return response.data;
  },

  closeTicket: async (ticketRef: string, adminId?: string): Promise<SupportTicketDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<SupportTicketDto>(
      ENDPOINTS.ADMIN_TICKET_CLOSE(ticketRef),
      {},
      { headers }
    );
    return response.data;
  },

  approveReplacement: async (replacementRef: string, notes?: string, adminId?: string): Promise<ReplacementRequestDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<ReplacementRequestDto>(
      ENDPOINTS.ADMIN_REPLACEMENT_APPROVE(replacementRef),
      { notes },
      { headers }
    );
    return response.data;
  }
};
