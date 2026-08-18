import { FC } from 'react';
import { Category } from '../types/catalog';
import { CategoryCard } from './CategoryCard';
import { Grid } from '../../../components/layout/Grid';

export interface CategoryGridProps {
  categories: Category[];
  className?: string;
}

export const CategoryGrid: FC<CategoryGridProps> = ({ categories, className = '' }) => {
  return (
    <Grid minWidth="260px" gap="1.5rem" className={`category-grid ${className}`}>
      {categories.map((category) => (
        <CategoryCard key={category.id} category={category} />
      ))}
    </Grid>
  );
};
