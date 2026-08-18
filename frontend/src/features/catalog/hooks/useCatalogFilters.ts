import { useState, useCallback } from 'react';
import { CatalogFilterState, ProductStatus } from '../types/catalog';
import { DEFAULT_CATALOG_FILTERS } from '../constants/catalogConstants';

export const useCatalogFilters = (initialFilters: Partial<CatalogFilterState> = {}) => {
  const [filters, setFilters] = useState<CatalogFilterState>({
    ...DEFAULT_CATALOG_FILTERS,
    ...initialFilters,
  });

  const setSearch = useCallback((search: string) => {
    setFilters((prev) => ({ ...prev, search, page: 0 }));
  }, []);

  const setCategory = useCallback((categoryId: string) => {
    setFilters((prev) => ({ ...prev, categoryId, page: 0 }));
  }, []);

  const setStatus = useCallback((status: ProductStatus | '') => {
    setFilters((prev) => ({ ...prev, status, page: 0 }));
  }, []);

  const setPriceRange = useCallback((minPrice?: number, maxPrice?: number) => {
    setFilters((prev) => ({ ...prev, minPrice, maxPrice, page: 0 }));
  }, []);

  const setSort = useCallback((sort: string) => {
    setFilters((prev) => ({ ...prev, sort, page: 0 }));
  }, []);

  const setPage = useCallback((page: number) => {
    setFilters((prev) => ({ ...prev, page }));
  }, []);

  const resetFilters = useCallback(() => {
    setFilters(DEFAULT_CATALOG_FILTERS);
  }, []);

  return {
    filters,
    setSearch,
    setCategory,
    setStatus,
    setPriceRange,
    setSort,
    setPage,
    resetFilters,
  };
};
