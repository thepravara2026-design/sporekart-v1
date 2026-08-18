import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import {
  primitiveColors,
  semanticColors,
  fontFamilies,
  fontSizes,
  radii,
  shadows,
  spacing,
  zIndex,
} from '../tokens';
import { DesignSystemShowcase } from '../../pages/DesignSystemShowcase';
import { ToastProvider } from '../../components/ui/Toast';

describe('Design System Tokens Unit Tests', () => {
  it('exports valid primitive and semantic color tokens', () => {
    expect(primitiveColors.forest[950]).toBe('#051c14');
    expect(primitiveColors.forest[500]).toBe('#10b981');
    expect(semanticColors.background.primary).toBe('#051c14');
    expect(semanticColors.action.primary).toBe('#10b981');
    expect(semanticColors.status.success).toBe('#10b981');
    expect(semanticColors.status.error).toBe('#ef4444');
  });

  it('exports valid typography tokens with Kannada locale support', () => {
    expect(fontFamilies.sans).toContain('Inter');
    expect(fontFamilies.kannada).toContain('Noto Sans Kannada');
    expect(fontSizes['4xl'][0]).toBe('2.25rem');
  });

  it('exports 4px base spacing scale, radii, and shadow tokens', () => {
    expect(spacing[1]).toBe('0.25rem');
    expect(spacing[4]).toBe('1rem');
    expect(radii.md).toBe('0.5rem');
    expect(radii.full).toBe('9999px');
    expect(shadows.glow).toBeDefined();
  });

  it('exports z-index layering scale', () => {
    expect(zIndex.base).toBe(0);
    expect(zIndex.modal).toBe(40);
    expect(zIndex.tooltip).toBe(60);
    expect(zIndex.modal).toBeLessThan(zIndex.toast);
  });

  it('renders DesignSystemShowcase validation component cleanly', () => {
    render(
      <ToastProvider>
        <DesignSystemShowcase />
      </ToastProvider>
    );
    expect(screen.getByText('Sporekart Design System & Shared UI Components')).toBeInTheDocument();
  });
});
