import { z } from 'zod';

export const StockAdjustmentSchema = z.object({
  sku: z.string().min(1, 'SKU is required'),
  newOnHandQuantity: z.number().int().min(0, 'Quantity cannot be negative'),
  reason: z.string().min(3, 'Reason must be at least 3 characters'),
});

export const GrowerProfileSchema = z.object({
  businessName: z.string().min(2, 'Business name must be at least 2 characters'),
  contactName: z.string().min(2, 'Contact name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  phone: z.string().optional(),
  address: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  zipCode: z.string().optional(),
  bio: z.string().max(500, 'Bio cannot exceed 500 characters').optional(),
});

export const GrowerProductSchema = z.object({
  name: z.string().min(2, 'Product name must be at least 2 characters'),
  sku: z.string().min(2, 'SKU must be at least 2 characters'),
  price: z.number().positive('Price must be greater than zero'),
  description: z.string().optional(),
  categoryId: z.string().optional(),
  status: z.enum(['ACTIVE', 'INACTIVE', 'DRAFT', 'ARCHIVED']),
});
