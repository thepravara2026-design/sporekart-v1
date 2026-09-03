import type { TrainingHighlight, GrowerStory, TrustSignal } from '../types/marketing';

// ──────────────────────────────────────────────
// Training section static marketing content
// These are presentation-layer items, not fabricated facts.
// Connect to a real training API when available.
// ──────────────────────────────────────────────
export const TRAINING_HIGHLIGHTS: TrainingHighlight[] = [
  {
    id: 'th-1',
    icon: 'Microscope',
    title: 'Spawn Science Fundamentals',
    description: 'Master sterile technique, agar work, and liquid culture preparation from scratch.',
  },
  {
    id: 'th-2',
    icon: 'Leaf',
    title: 'Substrate Engineering',
    description: 'Formulate high-yield hardwood, straw, and supplemented substrates for commercial runs.',
  },
  {
    id: 'th-3',
    icon: 'BookOpen',
    title: 'Commercial Cultivation Workflow',
    description: 'Scale from hobby grows to production-grade fruiting blocks with consistent results.',
  },
  {
    id: 'th-4',
    icon: 'HeartHandshake',
    title: 'Grower Mentorship',
    description: 'Access direct 1-on-1 sessions with experienced mycologists at SPOREKART.',
  },
];

// ──────────────────────────────────────────────
// Trust signals — verified product/business claims only
// ──────────────────────────────────────────────
export const TRUST_SIGNALS: TrustSignal[] = [
  {
    id: 'ts-1',
    icon: 'Sprout',
    title: 'High-Yield Genetics',
    description: 'Curated spawn strains selected for consistent flush performance in Indian climate conditions.',
  },
  {
    id: 'ts-2',
    icon: 'Truck',
    title: 'Cold-Chain Express Delivery',
    description: 'Temperature-controlled packaging to preserve culture viability from warehouse to your door.',
  },
  {
    id: 'ts-3',
    icon: 'ShieldCheck',
    title: 'Batch Viability Guarantee',
    description: 'Every batch is tested before dispatch. Non-viable batches are replaced at no charge.',
  },
  {
    id: 'ts-4',
    icon: 'Headphones',
    title: 'Expert Cultivator Support',
    description: 'Technical mycology help from professionals via the integrated SPOREKART support platform.',
  },
  {
    id: 'ts-5',
    icon: 'Lock',
    title: 'Secure Checkout',
    description: 'Industry-standard encryption and secure payment processing on every transaction.',
  },
  {
    id: 'ts-6',
    icon: 'RefreshCw',
    title: 'Easy Returns',
    description: 'Hassle-free return process managed through the SPOREKART returns centre.',
  },
];

// ──────────────────────────────────────────────
// Grower Stories — PRESENTATION LAYER ONLY
// These are illustrative story cards, NOT fabricated testimonials.
// Replace with real stories fetched from an API when available.
// ──────────────────────────────────────────────
export const GROWER_STORIES: GrowerStory[] = [
  {
    id: 'gs-1',
    title: 'From Kitchen Table to 200-Block Operation',
    excerpt:
      'How a home cultivator in Bengaluru scaled a side project into a sustainable small-farm enterprise using SPOREKART spawn and substrate kits.',
    category: 'Small Farm',
    ctaLabel: 'Read story',
    ctaPath: '/products',
  },
  {
    id: 'gs-2',
    title: 'Medicinal Mushroom Research at University Scale',
    excerpt:
      "A post-graduate mycology lab partnered with SPOREKART for consistent lion's mane and reishi cultures for academic research protocols.",
    category: 'Research',
    ctaLabel: 'Browse medicinal strains',
    ctaPath: '/products',
  },
  {
    id: 'gs-3',
    title: 'Training to First Profitable Flush in 90 Days',
    excerpt:
      'A first-time grower from Kerala documented the full journey from SPOREKART spawn science training through first commercial harvest.',
    category: 'Grower Journey',
    ctaLabel: 'Explore products',
    ctaPath: '/products',
  },
];

// ──────────────────────────────────────────────
// FAQ content — centralized, single source of truth
// ──────────────────────────────────────────────
export const FAQ_ITEMS = [
  {
    id: 'faq-1',
    category: 'Products',
    question: 'What types of mushroom spawn does SPOREKART stock?',
    answer:
      "SPOREKART offers oyster (grey, pink, yellow, blue), button (Agaricus), shiitake, lion's mane, reishi, and specialty medicinal spawn — available as grain spawn, sawdust spawn, and liquid cultures. Inventory is managed live through the product catalog.",
  },
  {
    id: 'faq-2',
    category: 'Products',
    question: 'How do I know which strain is suitable for my climate?',
    answer:
      'Each product listing includes cultivation environment notes. You can also filter by category and use the SPOREKART cultivator support channel for personalised strain recommendations.',
  },
  {
    id: 'faq-3',
    category: 'Ordering',
    question: 'What is the minimum order quantity?',
    answer:
      'There is no minimum quantity requirement. You can order a single spawn bag or a commercial pallet — the cart and checkout system handles both.',
  },
  {
    id: 'faq-4',
    category: 'Delivery',
    question: 'How does SPOREKART ship live spawn without contamination?',
    answer:
      'All spawn ships in sterile sealed packaging inside cold-chain-compliant boxes. Orders are dispatched on Mondays and Thursdays to minimise transit time over weekends.',
  },
  {
    id: 'faq-5',
    category: 'Delivery',
    question: 'Which regions does SPOREKART deliver to?',
    answer:
      'SPOREKART delivers across India. Delivery timelines and carrier options are shown at checkout based on your delivery address.',
  },
  {
    id: 'faq-6',
    category: 'Payments',
    question: 'What payment methods are accepted?',
    answer:
      'The checkout system supports UPI, net banking, major debit/credit cards, and wallets. All transactions are processed securely.',
  },
  {
    id: 'faq-7',
    category: 'Training',
    question: 'Are training programs suitable for beginners?',
    answer:
      'Yes. SPOREKART training is structured from foundational spawn science through to advanced commercial workflow. No prior mycology experience is required for entry-level workshops.',
  },
  {
    id: 'faq-8',
    category: 'Support',
    question: 'What if my spawn arrives contaminated or non-viable?',
    answer:
      'Contact SPOREKART support via the support portal immediately with batch details and photos. All batches carry a viability guarantee — non-viable stock is replaced or refunded.',
  },
];
