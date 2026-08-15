import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';

export interface MerchantReplyDto {
  id: string;
  reviewId: string;
  authorId: string;
  replyText: string;
  createdAt: string;
}

export interface ProductReviewDto {
  id: string;
  reviewReference: string;
  productId: string;
  orderId: string;
  orderItemId: string;
  customerId: string;
  rating: number;
  qualityRating?: number;
  valueRating?: number;
  title: string;
  comment: string;
  isVerifiedPurchase: boolean;
  status: 'PENDING_MODERATION' | 'APPROVED' | 'REJECTED' | 'FLAGGED';
  rejectionReason?: string;
  helpfulCount: number;
  unhelpfulCount: number;
  mediaUrls?: string;
  createdAt: string;
  updatedAt: string;
  merchantReplies: MerchantReplyDto[];
}

export interface ProductRatingSummaryDto {
  productId: string;
  averageRating: number;
  totalReviewsCount: number;
  star1Count: number;
  star2Count: number;
  star3Count: number;
  star4Count: number;
  star5Count: number;
  verifiedPurchaseCount: number;
  updatedAt: string;
}

export const reviewApi = {
  submitReview: async (
    payload: {
      productId: string;
      orderId: string;
      orderItemId: string;
      rating: number;
      qualityRating?: number;
      valueRating?: number;
      title: string;
      comment: string;
      mediaUrls?: string;
    },
    customerId: string
  ): Promise<ProductReviewDto> => {
    const response = await axiosInstance.post<ProductReviewDto>(
      ENDPOINTS.CUSTOMER_REVIEWS,
      payload,
      { headers: { 'X-Customer-Id': customerId } }
    );
    return response.data;
  },

  listProductReviews: async (productId: string): Promise<ProductReviewDto[]> => {
    const response = await axiosInstance.get<ProductReviewDto[]>(
      ENDPOINTS.PRODUCT_REVIEWS(productId)
    );
    return response.data;
  },

  getRatingSummary: async (productId: string): Promise<ProductRatingSummaryDto> => {
    const response = await axiosInstance.get<ProductRatingSummaryDto>(
      ENDPOINTS.PRODUCT_RATING_SUMMARY(productId)
    );
    return response.data;
  },

  voteHelpfulness: async (
    reviewRef: string,
    isHelpful: boolean,
    customerId?: string
  ): Promise<ProductReviewDto> => {
    const headers = customerId ? { 'X-Customer-Id': customerId } : undefined;
    const response = await axiosInstance.post<ProductReviewDto>(
      ENDPOINTS.REVIEW_VOTE(reviewRef),
      { isHelpful },
      { headers }
    );
    return response.data;
  },

  listAdminReviews: async (filter?: {
    status?: string;
    productId?: string;
    rating?: number;
  }): Promise<ProductReviewDto[]> => {
    const response = await axiosInstance.get<ProductReviewDto[]>(
      ENDPOINTS.ADMIN_REVIEWS,
      { params: filter }
    );
    return response.data;
  },

  approveReview: async (reviewRef: string, adminId?: string): Promise<ProductReviewDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<ProductReviewDto>(
      ENDPOINTS.ADMIN_REVIEW_APPROVE(reviewRef),
      {},
      { headers }
    );
    return response.data;
  },

  rejectReview: async (reviewRef: string, reason?: string, adminId?: string): Promise<ProductReviewDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<ProductReviewDto>(
      ENDPOINTS.ADMIN_REVIEW_REJECT(reviewRef),
      { reason },
      { headers }
    );
    return response.data;
  },

  flagReview: async (reviewRef: string, adminId?: string): Promise<ProductReviewDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<ProductReviewDto>(
      ENDPOINTS.ADMIN_REVIEW_FLAG(reviewRef),
      {},
      { headers }
    );
    return response.data;
  },

  addMerchantReply: async (reviewRef: string, replyText: string, adminId?: string): Promise<MerchantReplyDto> => {
    const headers = adminId ? { 'X-Admin-Id': adminId } : undefined;
    const response = await axiosInstance.post<MerchantReplyDto>(
      ENDPOINTS.ADMIN_REVIEW_REPLY(reviewRef),
      { replyText },
      { headers }
    );
    return response.data;
  }
};
