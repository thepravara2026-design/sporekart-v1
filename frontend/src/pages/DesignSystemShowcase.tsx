import { FC, useState, useRef } from 'react';
import {
  primitiveColors,
  semanticColors,
  fontFamilies,
  fontSizes,
  radii,
  shadows,
} from '../design-system/tokens';
import {
  Sparkles,
  Search,
  CheckCircle,
  AlertTriangle,
  Info,
  Shield,
  Layers,
  Palette,
  Type,
  Bell,
  Mail,
  MoreVertical,
  Plus,
} from 'lucide-react';
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
  useToast,
  EmptyState,
} from '../components/ui';

export const DesignSystemShowcase: FC = () => {
  const [activeTab, setActiveTab] = useState<'components' | 'forms' | 'overlays' | 'tokens'>('components');

  // Form states
  const [inputText, setInputText] = useState('');
  const [textareaText, setTextareaText] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('spawn');
  const [checkboxChecked, setCheckboxChecked] = useState(true);
  const [radioValue, setRadioValue] = useState('standard');
  const [switchChecked, setSwitchChecked] = useState(true);

  // Overlay states
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const dialogTriggerRef = useRef<HTMLButtonElement>(null);
  const drawerTriggerRef = useRef<HTMLButtonElement>(null);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);

  // Toast hook
  const { addToast } = useToast();

  return (
    <div style={{ padding: '2rem 1.5rem', maxWidth: '1200px', margin: '0 auto' }}>
      <header style={{ marginBottom: '2.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
          <Sparkles style={{ color: primitiveColors.forest[500], width: '28px', height: '28px' }} />
          <h1 style={{ fontSize: '2.25rem', fontWeight: 800, color: semanticColors.text.primary }}>
            Sporekart Design System & Shared UI Components
          </h1>
        </div>
        <p style={{ color: semanticColors.text.secondary, fontSize: '1rem' }}>
          Production-Grade Accessible Shared UI Primitives Validation Surface (Sprint FD-05)
        </p>
      </header>

      {/* Showcase Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '2rem', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '0.75rem', flexWrap: 'wrap' }}>
        <button
          onClick={() => setActiveTab('components')}
          className={`btn ${activeTab === 'components' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Layers size={16} /> Buttons, Badges & Alerts
        </button>
        <button
          onClick={() => setActiveTab('forms')}
          className={`btn ${activeTab === 'forms' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Type size={16} /> Form Controls & Fields
        </button>
        <button
          onClick={() => setActiveTab('overlays')}
          className={`btn ${activeTab === 'overlays' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Shield size={16} /> Overlays, Navigation & Feedback
        </button>
        <button
          onClick={() => setActiveTab('tokens')}
          className={`btn ${activeTab === 'tokens' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Palette size={16} /> Design Tokens & Palettes
        </button>
      </div>

      {/* TAB 1: BUTTONS, BADGES & ALERTS */}
      {activeTab === 'components' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <Card>
            <CardHeader>
              <CardTitle>Button System & Variants</CardTitle>
              <CardDescription>Native HTML button semantics with primary, secondary, outline, ghost, destructive, and link styles.</CardDescription>
            </CardHeader>
            <CardContent style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
                <Button variant="primary">Primary Action</Button>
                <Button variant="secondary">Secondary Action</Button>
                <Button variant="outline">Outline Action</Button>
                <Button variant="ghost">Ghost Action</Button>
                <Button variant="destructive">Destructive Action</Button>
                <Button variant="link">Link Button</Button>
              </div>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
                <Button size="sm">Small (sm)</Button>
                <Button size="md">Medium (md)</Button>
                <Button size="lg">Large (lg)</Button>
                <Button isLoading>Loading State</Button>
                <Button disabled>Disabled State</Button>
              </div>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
                <IconButton icon={<Search size={18} />} aria-label="Search items" />
                <IconButton icon={<Bell size={18} />} aria-label="Notifications" variant="primary" />
                <IconButton icon={<Mail size={18} />} aria-label="Messages" variant="outline" />
              </div>
            </CardContent>
            <CardFooter>
              <Button size="sm" variant="secondary">Reset Variants</Button>
            </CardFooter>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Status Badges & Accessible Alerts</CardTitle>
              <CardDescription>Semantically communicated status indicators and accessible alert banners.</CardDescription>
            </CardHeader>
            <CardContent style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                <Badge variant="success" icon={<CheckCircle size={14} />}>In Stock</Badge>
                <Badge variant="warning" icon={<AlertTriangle size={14} />}>Low Inventory</Badge>
                <Badge variant="danger">Out of Stock</Badge>
                <Badge variant="info" icon={<Info size={14} />}>Express Cold-Chain</Badge>
                <Badge variant="neutral">Draft Batch</Badge>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <Alert variant="info" title="Cold-Chain Express Shipping Active">
                  All mushroom spawn orders are dispatched in refrigerated temperature-monitored containers.
                </Alert>
                <Alert variant="success" title="Batch Registration Confirmed">
                  Batch #MB-2026-08 has been scheduled for incubation with 100 available seats.
                </Alert>
                <Alert variant="warning" title="Substrate Moisture Warning">
                  Relative humidity in Chamber 3 dropped below 85% optimal threshold.
                </Alert>
                <Alert variant="error" title="Payment Authorization Required">
                  Your payment method requires additional grower verification before shipment.
                </Alert>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* TAB 2: FORM CONTROLS */}
      {activeTab === 'forms' && (
        <Card>
          <CardHeader>
            <CardTitle>Form Controls & Accessible FormField System</CardTitle>
            <CardDescription>Inputs, textareas, selects, checkboxes, radio groups, and switches with explicit aria association.</CardDescription>
          </CardHeader>
          <CardContent style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
            <FormField label="Grower Full Name" htmlFor="input-name" required description="Enter your legal or farm business name">
              <Input
                id="input-name"
                placeholder="e.g. Preetham Bio Farms"
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
              />
            </FormField>

            <FormField label="Spawn Strain Category" htmlFor="select-category" required>
              <Select
                id="select-category"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                options={[
                  { value: 'spawn', label: 'Oyster Mushroom Spawn' },
                  { value: 'button', label: 'Button Mushroom Spawn' },
                  { value: 'milky', label: 'Milky Mushroom Culture' },
                ]}
              />
            </FormField>

            <FormField label="Substrate Composition Notes" htmlFor="textarea-notes" error={textareaText.length > 100 ? 'Notes cannot exceed 100 characters' : undefined}>
              <Textarea
                id="textarea-notes"
                placeholder="Specify paddy straw, sawdust, or wheat bran composition..."
                value={textareaText}
                invalid={textareaText.length > 100}
                onChange={(e) => setTextareaText(e.target.value)}
              />
            </FormField>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <Checkbox
                label="Subscribe to Spawn Batch Moisture Alerts"
                description="Receive SMS notifications when batch humidity fluctuates"
                checked={checkboxChecked}
                onChange={(e) => setCheckboxChecked(e.target.checked)}
              />

              <Switch
                label="Enable Cold-Chain Priority Logistics"
                description="Guarantees delivery within 24 hours of spawn harvest"
                checked={switchChecked}
                onChange={setSwitchChecked}
              />

              <RadioGroup
                name="logistics-speed"
                label="Logistics Delivery Service Level"
                value={radioValue}
                onChange={setRadioValue}
                options={[
                  { value: 'standard', label: 'Standard Express (2-3 Days)' },
                  { value: 'coldchain', label: 'Cold-Chain Refrigerated (24h Guaranteed)' },
                ]}
              />
            </div>
          </CardContent>
        </Card>
      )}

      {/* TAB 3: OVERLAYS, NAVIGATION & FEEDBACK */}
      {activeTab === 'overlays' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <Card>
            <CardHeader>
              <CardTitle>Dialog & Drawer Primitives</CardTitle>
              <CardDescription>Modal dialogs and off-canvas drawers with focus trap, Escape key handlers, and focus restoration.</CardDescription>
            </CardHeader>
            <CardContent style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <Button ref={dialogTriggerRef} onClick={() => setIsDialogOpen(true)}>
                Open Modal Dialog
              </Button>
              <Button ref={drawerTriggerRef} variant="secondary" onClick={() => setIsDrawerOpen(true)}>
                Open Off-Canvas Drawer
              </Button>
              <Button
                variant="outline"
                onClick={() => addToast({ title: 'Batch Spawn Scheduled', message: 'Cold-chain dispatch assigned for tomorrow morning.', variant: 'success' })}
              >
                Trigger Success Toast Notification
              </Button>

              <Dialog
                isOpen={isDialogOpen}
                onClose={() => setIsDialogOpen(false)}
                title="Schedule Mushroom Spawn Batch"
                description="Configure incubation parameters and seat allocation for upcoming grower training."
                triggerRef={dialogTriggerRef}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <FormField label="Batch Capacity Limit">
                    <Input type="number" defaultValue="25" />
                  </FormField>
                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
                    <Button variant="secondary" onClick={() => setIsDialogOpen(false)}>Cancel</Button>
                    <Button variant="primary" onClick={() => setIsDialogOpen(false)}>Confirm Schedule</Button>
                  </div>
                </div>
              </Dialog>

              <Drawer
                isOpen={isDrawerOpen}
                onClose={() => setIsDrawerOpen(false)}
                title="Grower Profile & Settings"
                triggerRef={drawerTriggerRef}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                    Manage your regional farm details, contact preferences, and enrollment history.
                  </p>
                  <Button variant="primary" fullWidth onClick={() => setIsDrawerOpen(false)}>Save Profile Changes</Button>
                </div>
              </Drawer>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Breadcrumbs, Dropcrumbs & Accessible Tabs</CardTitle>
              <CardDescription>Semantic navigation primitives and tabbed content regions.</CardDescription>
            </CardHeader>
            <CardContent style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <Breadcrumb
                items={[
                  { label: 'Home', path: '/' },
                  { label: 'Spawn Catalog', path: '/products' },
                  { label: 'Florida White Oyster Spawn' },
                ]}
              />

              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Dropdown Actions Menu:</span>
                <DropdownMenu
                  trigger={
                    <Button variant="secondary" size="sm" rightIcon={<MoreVertical size={16} />}>
                      Manage Batch Actions
                    </Button>
                  }
                  items={[
                    { id: 'edit', label: 'Edit Batch Capacity', icon: <Plus size={14} />, onClick: () => alert('Edit batch clicked') },
                    { id: 'export', label: 'Export Attendee List', onClick: () => alert('Export clicked') },
                    { id: 'delete', label: 'Cancel Scheduled Batch', danger: true, onClick: () => alert('Delete clicked') },
                  ]}
                />

                <Tooltip content="Dispatches refrigerated cold-chain courier within 2 hours">
                  <Badge variant="info">Priority Shipping Info</Badge>
                </Tooltip>
              </div>

              <Tabs
                items={[
                  { id: 'overview', label: 'Overview', content: <p style={{ color: 'var(--text-secondary)' }}>High-yield Florida White Oyster spawn grown on sterile millet substrate.</p> },
                  { id: 'specs', label: 'Technical Specs', content: <p style={{ color: 'var(--text-secondary)' }}>Incubation temp: 24-28°C | Fruiting humidity: 85-95% RH</p> },
                  { id: 'shipping', label: 'Shipping Policy', content: <p style={{ color: 'var(--text-secondary)' }}>Cold-chain express shipping with ice packs included.</p> },
                ]}
              />

              <Pagination currentPage={currentPage} totalPages={5} onPageChange={setCurrentPage} />
            </CardContent>
          </Card>

          <EmptyState
            title="No Spawn Batches Scheduled"
            description="You have not scheduled any upcoming mushroom spawn incubation batches yet."
            action={<Button variant="primary" leftIcon={<Plus size={16} />}>Schedule First Batch</Button>}
          />
        </div>
      )}

      {/* TAB 4: DESIGN TOKENS */}
      {activeTab === 'tokens' && (
        <Card>
          <CardHeader>
            <CardTitle>Forest Dark Color Palette & Typography Tokens</CardTitle>
            <CardDescription>Primary forest greens, bio emerald accents, and typography scale.</CardDescription>
          </CardHeader>
          <CardContent style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: '1rem' }}>
              {Object.entries(primitiveColors.forest).map(([shade, hex]) => (
                <div
                  key={shade}
                  style={{
                    background: hex,
                    borderRadius: radii.md,
                    padding: '1rem',
                    color: parseInt(shade) > 500 ? '#fff' : '#051c14',
                    boxShadow: shadows.sm,
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    height: '90px',
                  }}
                >
                  <div style={{ fontWeight: 700, fontSize: '0.875rem' }}>Forest {shade}</div>
                  <div style={{ fontSize: '0.75rem', fontFamily: fontFamilies.mono }}>{hex}</div>
                </div>
              ))}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <span style={{ fontSize: '0.75rem', color: semanticColors.text.muted, textTransform: 'uppercase' }}>Kannada Locale Support (Noto Sans Kannada)</span>
                <h2 style={{ fontFamily: fontFamilies.kannada, fontSize: fontSizes['2xl'][0] as string, fontWeight: 700, color: primitiveColors.forest[400] }}>
                  ಸ್ಪೋರ್‌ಕಾರ್ಟ್ ಅಣಬೆ ಕೃಷಿ ತರಬೇತಿ ಮತ್ತು ಮಾರುಕಟ್ಟೆ
                </h2>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};
