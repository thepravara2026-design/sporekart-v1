import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { catalogApi } from '../catalogApi';
import { axiosInstance } from '../apiClient';
import { ENDPOINTS } from '../endpoints';
import { Product, Category, PageResponse } from '../../types/catalog';
import { ApiResponse } from '../../types/api';

vi.mock('../apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

describe('catalogApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  const mockCategory: Category = {
    id: 'cat-111',
    name: 'Medicinal Mushrooms',
    slug: 'medicinal-mushrooms',
    description: 'High purity medicinal mushroom cultures',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };

  const mockProduct: Product = {
    id: 'prod-222',
    sku: 'SKU-LION-001',
    name: "Lion's Mane Liquid Culture",
    description: 'Premium liquid culture syringe',
    price: 24.99,
    currency: 'USD',
    status: 'ACTIVE',
    category: mockCategory,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };

  const mockPageResponse: PageResponse<Product> = {
    content: [mockProduct],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
  };

  it('fetches products list with params', async () => {
    const apiResponse: ApiResponse<PageResponse<Product>> = {
      success: true,
      data: mockPageResponse,
    };
    (axiosInstance.get as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({ data: apiResponse });

    const params = { page: 0, size: 10, sort: 'name,asc', search: 'Lion' };
    const result = await catalogApi.getProducts(params);

    expect(axiosInstance.get).toHaveBeenCalledWith(ENDPOINTS.PRODUCTS, {
      params,
      signal: undefined,
    });
    expect(result).toEqual(apiResponse);
  });

  it('fetches product detail by ID', async () => {
    const apiResponse: ApiResponse<Product> = {
      success: true,
      data: mockProduct,
    };
    (axiosInstance.get as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({ data: apiResponse });

    const result = await catalogApi.getProduct('prod-222');

    expect(axiosInstance.get).toHaveBeenCalledWith(ENDPOINTS.PRODUCT_BY_ID('prod-222'), {
      signal: undefined,
    });
    expect(result).toEqual(apiResponse);
  });

  it('fetches categories list', async () => {
    const mockCategoryPage: PageResponse<Category> = {
      content: [mockCategory],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    };
    const apiResponse: ApiResponse<PageResponse<Category>> = {
      success: true,
      data: mockCategoryPage,
    };
    (axiosInstance.get as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({ data: apiResponse });

    const result = await catalogApi.getCategories({ page: 0 });

    expect(axiosInstance.get).toHaveBeenCalledWith(ENDPOINTS.CATEGORIES, {
      params: { page: 0 },
      signal: undefined,
    });
    expect(result).toEqual(apiResponse);
  });

  it('fetches category detail by ID', async () => {
    const apiResponse: ApiResponse<Category> = {
      success: true,
      data: mockCategory,
    };
    (axiosInstance.get as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({ data: apiResponse });

    const result = await catalogApi.getCategory('cat-111');

    expect(axiosInstance.get).toHaveBeenCalledWith(ENDPOINTS.CATEGORY_BY_ID('cat-111'), {
      signal: undefined,
    });
    expect(result).toEqual(apiResponse);
  });
});
