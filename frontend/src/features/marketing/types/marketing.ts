/** Training highlight item for the training marketing section */
export interface TrainingHighlight {
  id: string;
  icon: string;
  title: string;
  description: string;
}

/** A trust signal card used in the Trust section */
export interface TrustSignal {
  id: string;
  icon: string;
  title: string;
  description: string;
}

/**
 * Grower story card — PRESENTATION LAYER only.
 * These represent story formats/templates, not fabricated testimonials.
 * Replace `excerpt` with real API-fetched content when a stories endpoint is available.
 */
export interface GrowerStory {
  id: string;
  title: string;
  excerpt: string;
  category: string;
  ctaLabel: string;
  ctaPath: string;
  imageUrl?: string;
}

/** Single FAQ item */
export interface FAQItem {
  id: string;
  category: string;
  question: string;
  answer: string;
}
