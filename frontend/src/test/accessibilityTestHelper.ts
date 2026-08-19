import { expect } from 'vitest';

export interface AccessibilityAuditOptions {
  requireHeadings?: boolean;
  requireLandmarks?: boolean;
  requireFormLabels?: boolean;
}

export const assertAccessibilityBaseline = (
  container: HTMLElement,
  options: AccessibilityAuditOptions = {}
): void => {
  const { requireHeadings = false, requireLandmarks = false, requireFormLabels = false } = options;

  if (requireHeadings) {
    const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6');
    expect(headings.length).toBeGreaterThan(0);
  }

  if (requireLandmarks) {
    const landmarks = container.querySelectorAll('header, main, footer, nav, aside, [role="main"], [role="navigation"]');
    expect(landmarks.length).toBeGreaterThan(0);
  }

  if (requireFormLabels) {
    const inputs = container.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
      const id = input.getAttribute('id');
      const ariaLabel = input.getAttribute('aria-label');
      const ariaLabelledBy = input.getAttribute('aria-labelledby');
      const hasLabel = !!ariaLabel || !!ariaLabelledBy || (!!id && !!container.querySelector(`label[for="${id}"]`));
      expect(hasLabel).toBe(true);
    });
  }

  const buttons = container.querySelectorAll('button');
  buttons.forEach(button => {
    const hasText = !!button.textContent?.trim();
    const hasAriaLabel = !!button.getAttribute('aria-label');
    const hasTitle = !!button.getAttribute('title');
    expect(hasText || hasAriaLabel || hasTitle).toBe(true);
  });
};
