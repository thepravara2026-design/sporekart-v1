import { FC, useState } from 'react';
import { useGrowerSettings } from '../hooks/useGrowerSettings';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Input } from '../../../components/ui/Input';
import { Switch } from '../../../components/ui/Switch';
import { Alert } from '../../../components/ui/Alert';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';

export const GrowerSettingsPage: FC = () => {
  const { settings, isLoading, isError, updateSettings, isUpdating } = useGrowerSettings();
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [formState, setFormState] = useState<{
    emailNotifications?: boolean;
    lowStockAlertThreshold?: number;
    preferredCarrier?: string;
    defaultFulfillmentLocation?: string;
  }>({});

  if (isLoading) {
    return <GrowerSkeleton type="card" count={2} />;
  }

  if (isError || !settings) {
    return <GrowerErrorState />;
  }

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateSettings(formState);
      setSuccessMsg('Operational settings saved successfully.');
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch {
      // Handled in hook
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', maxWidth: '800px' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
          Grower Operational Settings
        </h1>
        <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
          Configure notification thresholds, dispatch location defaults, and carrier preferences.
        </p>
      </div>

      {successMsg && <Alert variant="success">{successMsg}</Alert>}

      <Card style={{ padding: '1.5rem', backgroundColor: '#0d231a', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
        <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', color: '#e5e7eb' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '1rem', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
            <div>
              <span style={{ fontSize: '0.9375rem', fontWeight: 600, display: 'block' }}>
                Email Stock Alerts
              </span>
              <span style={{ fontSize: '0.8125rem', color: '#9ca3af' }}>
                Receive instant emails when batch stock drops below reorder point.
              </span>
            </div>
            <Switch
              checked={formState.emailNotifications ?? settings.emailNotifications}
              onChange={(checked) => setFormState((prev) => ({ ...prev, emailNotifications: checked }))}
              label="Email Stock Alerts"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
                Low Stock Threshold
              </label>
              <Input
                type="number"
                min={1}
                defaultValue={settings.lowStockAlertThreshold}
                onChange={(e) => setFormState((prev) => ({ ...prev, lowStockAlertThreshold: parseInt(e.target.value, 10) || 10 }))}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
                Preferred Carrier Partner
              </label>
              <Input
                defaultValue={settings.preferredCarrier}
                onChange={(e) => setFormState((prev) => ({ ...prev, preferredCarrier: e.target.value }))}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
              Default Dispatch Facility Location
            </label>
            <Input
              defaultValue={settings.defaultFulfillmentLocation}
              onChange={(e) => setFormState((prev) => ({ ...prev, defaultFulfillmentLocation: e.target.value }))}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <Button type="submit" variant="primary" isLoading={isUpdating}>
              Save Settings
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
