# SPOREKART v3.0 — Shared UI Components Architecture (FD-05)

**Sprint:** FD-05 — Shared UI Components & Design System Primitives  
**Status:** Production / FAANG-Level Ready  
**Location:** `frontend/src/components/ui/`  

---

## 1. Overview & Conceptual Architecture

The SPOREKART Shared UI Component System establishes a reusable, composable, strongly typed, accessible, and token-driven component layer consumed across all future frontend feature work (catalog, checkout, cart, training, admin, and grower dashboard).

### Key Architectural Standards
1. **Design Token Integration:** All components consume Layer 1 and Layer 2 tokens (`var(--...)`, `primitiveColors`, `semanticColors`, `radii`, `shadows`, `spacing`, `zIndex`, `motion`).
2. **Zero Business Logic:** Shared UI components contain no domain API calls or business operations. Behavior is passed via standard React callbacks (`onClick`, `onChange`, `onSubmit`).
3. **Accessibility First (WCAG 2.2 AA):** All components enforce semantic HTML5, explicit ARIA roles/states (`role="dialog"`, `aria-modal="true"`, `aria-busy`, `aria-invalid`, `aria-describedby`, `aria-current="page"`), focus traps, and keyboard navigation (`Tab`, `Shift+Tab`, `Enter`, `Space`, `Escape`, `Arrow keys`, `Home`, `End`).
4. **Ref Forwarding & Controlled Usage:** Standard interactive controls support React `forwardRef` and controlled/uncontrolled state patterns.

---

## 2. Shared Component Catalogue

| Component | Export Path | Key Props / Variants | Accessibility & Semantics |
|---|---|---|---|
| **Button** | `src/components/ui/Button.tsx` | `variant`: `primary` \| `secondary` \| `outline` \| `ghost` \| `destructive` \| `link`<br/>`size`: `sm` \| `md` \| `lg`<br/>`isLoading`, `fullWidth`, `leftIcon`, `rightIcon` | Native `<button>`, `aria-busy` during loading, disabled state handling |
| **IconButton** | `src/components/ui/IconButton.tsx` | `icon`, `aria-label` (Mandatory), `variant`, `size`, `isLoading` | Icon-only control requiring explicit `aria-label` accessible name |
| **Input** | `src/components/ui/Input.tsx` | Native input types (`text`, `email`, `password`, `number`, `search`), `invalid` | Native `<input>`, `aria-invalid` support |
| **Textarea** | `src/components/ui/Textarea.tsx` | `rows`, `invalid`, `disabled` | Native `<textarea>`, `aria-invalid` |
| **Select** | `src/components/ui/Select.tsx` | `options`: `Array<{ value, label, disabled }>`, `invalid` | Native `<select>` wrapper, accessible focus ring |
| **Checkbox** | `src/components/ui/Checkbox.tsx` | `label`, `description`, `invalid`, `disabled` | Native `<input type="checkbox">` with associated `<label>` |
| **RadioGroup** | `src/components/ui/RadioGroup.tsx` | `name`, `options`, `value`, `onChange`, `label` | `<fieldset>` and `<legend>` container, native radio controls |
| **Switch** | `src/components/ui/Switch.tsx` | `checked`, `onChange`, `label`, `description`, `disabled` | `role="switch"`, `aria-checked`, `Space` / `Enter` keyboard toggle |
| **FormField** | `src/components/ui/FormField.tsx` | `label`, `description`, `error`, `required`, `htmlFor` | Explicit `htmlFor`, `id` association, `role="alert"` for error messages |
| **Card** | `src/components/ui/Card.tsx` | `Card`, `CardHeader`, `CardTitle`, `CardDescription`, `CardContent`, `CardFooter` | Composable surface primitive with glassmorphism backdrop blur |
| **Badge** | `src/components/ui/Badge.tsx` | `variant`: `default` \| `neutral` \| `success` \| `warning` \| `danger` \| `info`, `icon` | Status badges with visual text and icon support |
| **Alert** | `src/components/ui/Alert.tsx` | `title`, `children`, `variant`: `info` \| `success` \| `warning` \| `error`, `onDismiss` | `role="alert"` / `role="status"`, `aria-live` polite/assertive |
| **Dialog** | `src/components/ui/Dialog.tsx` | `isOpen`, `onClose`, `title`, `description`, `triggerRef` | Modal dialog, `role="dialog"`, `aria-modal="true"`, focus trap, `Escape` key handler |
| **Drawer** | `src/components/ui/Drawer.tsx` | `isOpen`, `onClose`, `title`, `position`: `left` \| `right`, `triggerRef` | Off-canvas panel, `role="dialog"`, `aria-modal="true"`, focus trap, `Escape` close |
| **DropdownMenu** | `src/components/ui/DropdownMenu.tsx` | `trigger`, `items`: `Array<{ id, label, onClick, disabled, danger }>` | `role="menu"`, `role="menuitem"`, `ArrowUp`/`ArrowDown`/`Home`/`End`/`Escape` navigation |
| **Tooltip** | `src/components/ui/Tooltip.tsx` | `content`, `children`, `position`: `top` \| `bottom` \| `left` \| `right` | `role="tooltip"`, `aria-describedby`, hover & focus triggers |
| **Tabs** | `src/components/ui/Tabs.tsx` | `items`: `Array<{ id, label, content, disabled }>`, `defaultTabId`, `onChange` | `role="tablist"`, `role="tab"`, `role="tabpanel"`, `aria-selected`, `ArrowLeft`/`ArrowRight` |
| **Breadcrumb** | `src/components/ui/Breadcrumb.tsx` | `items`: `Array<{ label, path }>`, `separator` | Semantic `<nav aria-label="Breadcrumb navigation">`, `aria-current="page"` |
| **Pagination** | `src/components/ui/Pagination.tsx` | `currentPage`, `totalPages`, `onPageChange` | `<nav aria-label="Pagination navigation">`, `aria-current="page"`, disabled boundary controls |
| **Toast** | `src/components/ui/Toast.tsx` | `ToastProvider`, `useToast()`, `addToast({ message, title, variant, duration })` | `zIndex.toast` fixed overlay, `aria-live="polite"`, `role="status"` notification system |
| **EmptyState** | `src/components/ui/EmptyState.tsx` | `icon`, `title`, `description`, `action` | Domain-neutral empty container for empty data queries |
| **Loading Primitives** | `src/components/ui/LoadingSpinner.tsx` & `Skeleton.tsx` | `size`, `label`, `width`, `height`, `shape` | Reused FD-04 primitives; `role="status"` for spinner, `aria-hidden="true"` for skeletons |

---

## 3. Usage Examples

### Form Field Composition
```tsx
import { FormField, Input, Select, Button } from '../components/ui';

export const GrowerForm = () => (
  <form style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
    <FormField label="Full Name" htmlFor="name" required description="Legal name for training certification">
      <Input id="name" placeholder="e.g. Anand Kumar" />
    </FormField>

    <FormField label="Spawn Category" htmlFor="category">
      <Select
        id="category"
        options={[
          { value: 'oyster', label: 'Florida White Oyster' },
          { value: 'button', label: 'Agaricus Bisporus' },
        ]}
      />
    </FormField>

    <Button type="submit" variant="primary">Submit Application</Button>
  </form>
);
```

---

## 4. Design System Validation Surface

All components are demonstrated across default, hover, focus, disabled, loading, error, success, and selected states at:
- **Route:** `/design-system-showcase`
- **File:** [`frontend/src/pages/DesignSystemShowcase.tsx`](file:///f:/sporekart-v3.0/frontend/src/pages/DesignSystemShowcase.tsx)
