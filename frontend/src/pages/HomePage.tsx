import { FC } from 'react';
import { HeroSection } from '../features/marketing/components/HeroSection';
import { FeaturedProducts } from '../features/marketing/components/FeaturedProducts';
import { CategorySection } from '../features/marketing/components/CategorySection';
import { TrainingSection } from '../features/marketing/components/TrainingSection';
import { TrustSection } from '../features/marketing/components/TrustSection';
import { GrowerStories } from '../features/marketing/components/GrowerStories';
import { FAQSection } from '../features/marketing/components/FAQSection';

/**
 * HomePage
 *
 * Composes marketing sections in order:
 * 1. Hero
 * 2. Featured Products (API-backed, isolated error boundary)
 * 3. Category Section (API-backed, isolated error boundary)
 * 4. Training (static marketing content)
 * 5. Trust (static verified claims)
 * 6. Grower Stories (presentation layer)
 * 7. FAQ (static centralized content)
 *
 * Each API-backed section handles its own loading/error/empty state independently.
 * A failure in one section does not affect adjacent sections.
 */
export const HomePage: FC = () => {
  return (
    <div data-testid="home-page">
      <HeroSection />
      <FeaturedProducts />
      <CategorySection />
      <TrainingSection />
      <TrustSection />
      <GrowerStories />
      <FAQSection />
    </div>
  );
};
