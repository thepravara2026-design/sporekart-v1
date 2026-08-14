import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';
import { ApiResponse } from '../types/api';
import { Product, Category, PageResponse, ProductQueryParams, CategoryQueryParams } from '../types/catalog';

export const catalogApi = {
  getProducts: async (
    params?: ProductQueryParams,
    signal?: AbortSignal
  ): Promise<ApiResponse<PageResponse<Product>>> => {
    const response = await axiosInstance.get<ApiResponse<PageResponse<Product>>>(ENDPOINTS.PRODUCTS, {
      params,
      signal,
    });
    return response.data;
  },

  getProduct: async (
    productId: string,
    signal?: AbortSignal
  ): Promise<ApiResponse<Product>> => {
    const response = await axiosInstance.get<ApiResponse<Product>>(
      ENDPOINTS.PRODUCT_BY_ID(productId),
      { signal }
    );
    return response.data;
  },

  getCategories: async (
    params?: CategoryQueryParams,
    signal?: AbortSignal
  ): Promise<ApiResponse<PageResponse<Category>>> => {
    const response = await axiosInstance.get<ApiResponse<PageResponse<Category>>>(
      ENDPOINTS.CATEGORIES,
      { params, signal }
    );
    return response.data;
  },

  getCategory: async (
    categoryId: string,
    signal?: AbortSignal
  ): Promise<ApiResponse<Category>> => {
    const response = await axiosInstance.get<ApiResponse<Category>>(
      ENDPOINTS.CATEGORY_BY_ID(categoryId),
      { signal }
    );
    return response.data;
  },
};
