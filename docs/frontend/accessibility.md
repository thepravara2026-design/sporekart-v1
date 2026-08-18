# FD-04 — Accessibility Architecture & WCAG 2.2 AA Compliance Standard

**Sprint:** FD-04  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Active Baseline  

---

## 1. Executive Summary

This document specifies the accessibility architecture and implementation guidelines for SPOREKART v3.0. The framework strictly adheres to WCAG 2.2 Level AA requirements, guaranteeing keyboard operability, screen reader landmark clarity, focus management, and non-visual state communication.

---

## 2. Accessibility Core Rules & Standards

### 2.1 Accessible HTML5 Landmarks
Every application page MUST render a clean landmark structure without duplicate unlabelled regions:
- **Banner:** `<header className="navbar" role="banner">`
- **Primary Navigation:** `<nav aria-label="Primary navigation">`
- **Main Content:** `<main id="main-content" tabIndex={-1}>`
- **Sidebar Navigation:** `<aside aria-label="Admin console sidebar navigation">`
- **Contentinfo (Footer):** `<footer role="contentinfo" aria-label="Site footer">`

### 2.2 Keyboard Skip Navigation
The `<SkipLink />` component is rendered as the first element in every layout. It is positioned offscreen (`top: -9999px`) until targeted by keyboard focus (`Tab`), at which point it shifts to `top: 1rem` with Bio-emerald background and skips directly to `#main-content`.

### 2.3 Mobile Navigation Drawer Accessibility
- Trigger button uses `aria-expanded={isOpen}`, `aria-controls="mobile-nav-drawer"`, and explicit `aria-label`.
- Opening the drawer focuses the close button (`.mobile-nav-close`).
- Pressing `Escape` or clicking the backdrop closes the drawer and restores keyboard focus to the hamburger trigger button (`triggerRef.current.focus()`).
- Background page scrolling is locked (`body.style.overflow = 'hidden'`) while drawer is open.

### 2.4 Focus-Visible Rings
All focusable elements (`<a>`, `<button>`, `<input>`, `<select>`) exhibit a high-contrast 2px bio-emerald outline offset (`:focus-visible`):
```css
:focus-visible {
  outline: 2px solid var(--forest-500);
  outline-offset: 2px;
}
```

### 2.5 Loading & Skeleton Semantics
- Active loading spinners use `role="status"` and explicit `aria-label="Loading..."` for screen readers.
- Purely visual skeleton placeholders use `aria-hidden="true"` to prevent screen reader noise during background data fetching.

### 2.6 Error Boundary Announcements
React error boundaries render fallback containers with `role="alert"` and `aria-live="assertive"`, ensuring immediate screen reader notification without exposing raw stack traces.
