import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Category } from '../types/catalog';
import { Card, CardHeader, CardTitle, CardDescription } from '../../../components/ui/Card';
import { Badge } from '../../../components/ui/Badge';
import { Sprout, ArrowRight } from 'lucide-react';

export interface CategoryCardProps {
  category: Category;
  className?: string;
}

export const CategoryCard: FC<CategoryCardProps> = ({ category, className = '' }) => {
  return (
    <Card className={`category-card ${className}`} style={{ height: '100%' }}>
      <CardHeader>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--accent-primary)' }}>
            <Sprout size={24} />
          </div>
          <Badge variant={category.status === 'ACTIVE' ? 'success' : 'neutral'}>
            {category.status}
          </Badge>
        </div>
        <CardTitle>{category.name}</CardTitle>
        {category.description && <CardDescription>{category.description}</CardDescription>}
      </CardHeader>
      <div style={{ marginTop: 'auto', paddingTop: '1.25rem' }}>
        <Link
          to={`/products?categoryId=${category.id}`}
          className="btn btn-secondary btn-sm"
          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', width: '100%', justifyContent: 'center' }}
        >
          Browse Products <ArrowRight size={16} />
        </Link>
      </div>
    </Card>
  );
};
