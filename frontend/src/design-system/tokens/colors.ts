export const primitiveColors = {
  forest: {
    50: '#ecfdf5',
    100: '#d1fae5',
    200: '#a7f3d0',
    300: '#6ee7b7',
    400: '#34d399',
    500: '#10b981', // Bio Emerald
    600: '#059669',
    700: '#047857',
    800: '#134233', // Glassmorphic Card Surface
    850: '#0f382a', // Secondary Surface
    900: '#0a291d', // Dark Emerald Container
    950: '#051c14', // Sporekart Primary Dark
  },
  emerald: {
    500: '#10b981',
    600: '#059669',
    700: '#047857',
  },
  slate: {
    50: '#f8fafc',
    100: '#f1f5f9',
    200: '#e2e8f0',
    300: '#cbd5e1',
    400: '#94a3b8',
    500: '#64748b',
    600: '#475569',
    700: '#334155',
    800: '#1e293b',
    900: '#0f172a',
  },
  amber: {
    500: '#f59e0b',
    600: '#d97706',
  },
  red: {
    500: '#ef4444',
    600: '#dc2626',
  },
  blue: {
    500: '#3b82f6',
    600: '#2563eb',
  },
  white: '#ffffff',
  black: '#000000',
};

export const semanticColors = {
  background: {
    primary: primitiveColors.forest[950],
    secondary: primitiveColors.forest[900],
    surface: primitiveColors.forest[850],
    card: 'rgba(19, 66, 51, 0.75)',
    cardHover: 'rgba(19, 66, 51, 0.90)',
    overlay: 'rgba(5, 28, 20, 0.80)',
  },
  text: {
    primary: '#f9fafb',
    secondary: '#9ca3af',
    muted: '#6b7280',
    inverse: '#051c14',
    accent: primitiveColors.forest[500],
  },
  border: {
    default: 'rgba(255, 255, 255, 0.1)',
    subtle: 'rgba(255, 255, 255, 0.05)',
    focus: primitiveColors.forest[500],
    error: primitiveColors.red[500],
  },
  action: {
    primary: primitiveColors.forest[500],
    primaryHover: primitiveColors.forest[600],
    secondary: 'rgba(255, 255, 255, 0.08)',
    secondaryHover: 'rgba(255, 255, 255, 0.15)',
    disabled: 'rgba(255, 255, 255, 0.2)',
  },
  status: {
    success: primitiveColors.forest[500],
    successBg: 'rgba(16, 185, 129, 0.15)',
    warning: primitiveColors.amber[500],
    warningBg: 'rgba(245, 158, 11, 0.15)',
    error: primitiveColors.red[500],
    errorBg: 'rgba(239, 68, 68, 0.15)',
    info: primitiveColors.blue[500],
    infoBg: 'rgba(59, 130, 246, 0.15)',
  },
};
