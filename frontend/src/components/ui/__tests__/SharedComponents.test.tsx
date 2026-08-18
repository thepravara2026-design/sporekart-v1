import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import {
  Button,
  IconButton,
  Input,
  Textarea,
  Select,
  Checkbox,
  RadioGroup,
  Switch,
  FormField,
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
  Badge,
  Alert,
  Dialog,
  Drawer,
  DropdownMenu,
  Tooltip,
  Tabs,
  Breadcrumb,
  Pagination,
  ToastProvider,
  useToast,
  EmptyState,
} from '../index';
import { Search } from 'lucide-react';

describe('Shared UI Components System (Sprint FD-05)', () => {
  describe('Button & IconButton', () => {
    it('renders Button with variants and sizes correctly', () => {
      const handleClick = vi.fn();
      render(<Button variant="primary" size="lg" onClick={handleClick}>Click Me</Button>);
      const button = screen.getByRole('button', { name: 'Click Me' });
      expect(button).toBeDefined();
      fireEvent.click(button);
      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('handles Button loading state and aria-busy attribute', () => {
      render(<Button isLoading>Submitting</Button>);
      const button = screen.getByRole('button');
      expect(button.getAttribute('aria-busy')).toBe('true');
      expect(button.hasAttribute('disabled')).toBe(true);
    });

    it('renders IconButton with mandatory accessible name', () => {
      const handleClick = vi.fn();
      render(<IconButton icon={<Search size={16} />} aria-label="Search Catalog" onClick={handleClick} />);
      const btn = screen.getByRole('button', { name: 'Search Catalog' });
      expect(btn).toBeDefined();
      fireEvent.click(btn);
      expect(handleClick).toHaveBeenCalledTimes(1);
    });
  });

  describe('Form Controls & FormField', () => {
    it('renders Input and handles value change', () => {
      const handleChange = vi.fn();
      render(<Input placeholder="Enter username" onChange={handleChange} invalid />);
      const input = screen.getByPlaceholderText('Enter username');
      expect(input.getAttribute('aria-invalid')).toBe('true');
      fireEvent.change(input, { target: { value: 'grower1' } });
      expect(handleChange).toHaveBeenCalled();
    });

    it('renders Textarea with invalid styling', () => {
      render(<Textarea placeholder="Notes" invalid />);
      const textarea = screen.getByPlaceholderText('Notes');
      expect(textarea.getAttribute('aria-invalid')).toBe('true');
    });

    it('renders Select with options', () => {
      render(
        <Select options={[{ value: 'opt1', label: 'Option 1' }, { value: 'opt2', label: 'Option 2' }]} />
      );
      const select = screen.getByRole('combobox');
      expect(select).toBeDefined();
      expect(screen.getByText('Option 1')).toBeDefined();
    });

    it('renders Checkbox and handles click', () => {
      const handleChange = vi.fn();
      render(<Checkbox label="I agree to terms" onChange={handleChange} />);
      const checkbox = screen.getByRole('checkbox', { name: 'I agree to terms' });
      expect(checkbox).toBeDefined();
      fireEvent.click(checkbox);
      expect(handleChange).toHaveBeenCalled();
    });

    it('renders RadioGroup with options', () => {
      const handleChange = vi.fn();
      render(
        <RadioGroup
          name="shipping"
          label="Shipping Speed"
          value="standard"
          onChange={handleChange}
          options={[
            { value: 'standard', label: 'Standard' },
            { value: 'express', label: 'Express' },
          ]}
        />
      );
      expect(screen.getByText('Shipping Speed')).toBeDefined();
      const standardRadio = screen.getByRole('radio', { name: 'Standard' });
      expect(standardRadio.getAttribute('checked')).not.toBeNull();
    });

    it('renders Switch toggle with keyboard interaction', () => {
      const handleChange = vi.fn();
      render(<Switch label="Enable Notifications" checked={false} onChange={handleChange} />);
      const toggle = screen.getByRole('switch', { name: 'Enable Notifications' });
      expect(toggle.getAttribute('aria-checked')).toBe('false');
      fireEvent.keyDown(toggle, { key: ' ' });
      expect(handleChange).toHaveBeenCalledWith(true);
    });

    it('renders FormField with label, description, and error message', () => {
      render(
        <FormField label="Email Address" htmlFor="user-email" description="We never share your email" error="Invalid email format">
          <Input id="user-email" />
        </FormField>
      );
      expect(screen.getByText('Email Address')).toBeDefined();
      expect(screen.getByRole('alert')).toBeDefined();
      expect(screen.getByText('Invalid email format')).toBeDefined();
    });
  });

  describe('Card, Badge, Alert', () => {
    it('renders composable Card structure', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>Title</CardTitle>
            <CardDescription>Description</CardDescription>
          </CardHeader>
          <CardContent>Body</CardContent>
          <CardFooter>Footer</CardFooter>
        </Card>
      );
      expect(screen.getByText('Title')).toBeDefined();
      expect(screen.getByText('Description')).toBeDefined();
      expect(screen.getByText('Body')).toBeDefined();
    });

    it('renders Badge with variants', () => {
      render(<Badge variant="success">In Stock</Badge>);
      expect(screen.getByText('In Stock')).toBeDefined();
    });

    it('renders Alert with dismiss action', () => {
      const handleDismiss = vi.fn();
      render(<Alert title="Info Alert" variant="info" onDismiss={handleDismiss}>Info details</Alert>);
      expect(screen.getByRole('status')).toBeDefined();
      expect(screen.getByText('Info Alert')).toBeDefined();
      const dismissBtn = screen.getByRole('button', { name: 'Dismiss alert' });
      fireEvent.click(dismissBtn);
      expect(handleDismiss).toHaveBeenCalledTimes(1);
    });
  });

  describe('Overlays: Dialog & Drawer', () => {
    it('renders Dialog modal with role="dialog" and aria-modal="true"', () => {
      const handleClose = vi.fn();
      render(
        <Dialog isOpen={true} onClose={handleClose} title="Dialog Title" description="Dialog Desc">
          <p>Dialog Body</p>
        </Dialog>
      );
      const dialog = screen.getByRole('dialog');
      expect(dialog.getAttribute('aria-modal')).toBe('true');
      expect(screen.getByText('Dialog Title')).toBeDefined();

      const closeBtn = screen.getByRole('button', { name: 'Close dialog' });
      fireEvent.click(closeBtn);
      expect(handleClose).toHaveBeenCalled();
    });

    it('renders Drawer with left/right positioning', () => {
      const handleClose = vi.fn();
      render(
        <Drawer isOpen={true} onClose={handleClose} title="Drawer Title">
          <p>Drawer Body</p>
        </Drawer>
      );
      expect(screen.getByRole('dialog', { name: 'Drawer Title' })).toBeDefined();
    });
  });

  describe('Navigation & Feedback: DropdownMenu, Tooltip, Tabs, Breadcrumb, Pagination, Toast, EmptyState', () => {
    it('renders DropdownMenu and handles item clicks', () => {
      const handleItemClick = vi.fn();
      render(
        <DropdownMenu
          trigger={<Button>Options</Button>}
          items={[{ id: 'action1', label: 'Action 1', onClick: handleItemClick }]}
        />
      );
      const trigger = screen.getByRole('button', { name: 'Options' });
      fireEvent.click(trigger);
      const menuItem = screen.getByRole('menuitem');
      expect(menuItem).toBeDefined();
      fireEvent.click(menuItem);
      expect(handleItemClick).toHaveBeenCalled();
    });

    it('renders Tooltip on hover/focus', () => {
      render(
        <Tooltip content="Tooltip Content">
          <button>Hover Me</button>
        </Tooltip>
      );
      const button = screen.getByRole('button', { name: 'Hover Me' });
      fireEvent.mouseEnter(button.parentElement!);
      expect(screen.getByRole('tooltip')).toBeDefined();
      expect(screen.getByText('Tooltip Content')).toBeDefined();
    });

    it('renders Tabs with role="tablist" and switches active tab panel', () => {
      render(
        <Tabs
          items={[
            { id: 't1', label: 'Tab 1', content: <p>Panel 1</p> },
            { id: 't2', label: 'Tab 2', content: <p>Panel 2</p> },
          ]}
        />
      );
      expect(screen.getByRole('tablist')).toBeDefined();
      const tab2 = screen.getByRole('tab', { name: 'Tab 2' });
      fireEvent.click(tab2);
      expect(screen.getByText('Panel 2')).toBeDefined();
    });

    it('renders Breadcrumb navigation with aria-current="page"', () => {
      render(
        <BrowserRouter>
          <Breadcrumb items={[{ label: 'Home', path: '/' }, { label: 'Products' }]} />
        </BrowserRouter>
      );
      expect(screen.getByRole('navigation', { name: 'Breadcrumb navigation' })).toBeDefined();
      const current = screen.getByText('Products');
      expect(current.getAttribute('aria-current')).toBe('page');
    });

    it('renders Pagination and fires page change callback', () => {
      const handlePageChange = vi.fn();
      render(<Pagination currentPage={0} totalPages={3} onPageChange={handlePageChange} />);
      expect(screen.getByRole('navigation', { name: 'Pagination navigation' })).toBeDefined();
      const nextPageBtn = screen.getByRole('button', { name: 'Go to next page' });
      fireEvent.click(nextPageBtn);
      expect(handlePageChange).toHaveBeenCalledWith(1);
    });

    it('renders Toast notification via ToastProvider and useToast', () => {
      const TestToastComponent = () => {
        const { addToast } = useToast();
        return (
          <button onClick={() => addToast({ message: 'Operation successful', variant: 'success' })}>
            Trigger Toast
          </button>
        );
      };

      render(
        <ToastProvider>
          <TestToastComponent />
        </ToastProvider>
      );

      const btn = screen.getByRole('button', { name: 'Trigger Toast' });
      fireEvent.click(btn);
      expect(screen.getByRole('status')).toBeDefined();
      expect(screen.getByText('Operation successful')).toBeDefined();
    });

    it('renders EmptyState component with title, description, and action', () => {
      render(
        <EmptyState
          title="No Products Found"
          description="Try adjusting your filter search"
          action={<Button>Reset Filters</Button>}
        />
      );
      expect(screen.getByText('No Products Found')).toBeDefined();
      expect(screen.getByText('Try adjusting your filter search')).toBeDefined();
      expect(screen.getByRole('button', { name: 'Reset Filters' })).toBeDefined();
    });
  });
});
