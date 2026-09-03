import { primitiveColors, semanticColors } from './src/design-system/tokens/colors';
import { fontFamilies } from './src/design-system/tokens/typography';
import { radii } from './src/design-system/tokens/radii';
import { shadows } from './src/design-system/tokens/shadows';
import { zIndex } from './src/design-system/tokens/zIndex';

/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        forest: primitiveColors.forest,
        slate: primitiveColors.slate,
        brand: {
          bg: semanticColors.background.primary,
          surface: semanticColors.background.surface,
          card: semanticColors.background.card,
          accent: semanticColors.action.primary,
          accentHover: semanticColors.action.primaryHover,
        },
      },
      fontFamily: {
        sans: fontFamilies.sans.split(', '),
        kannada: fontFamilies.kannada.split(', '),
        mono: fontFamilies.mono.split(', '),
      },
      borderRadius: radii,
      boxShadow: shadows,
      zIndex: zIndex,
    },
  },
  plugins: [],
};
