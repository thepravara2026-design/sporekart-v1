import { FC, useState } from 'react';
import { useGrowerProfile } from '../hooks/useGrowerProfile';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Input } from '../../../components/ui/Input';
import { Textarea } from '../../../components/ui/Textarea';
import { Alert } from '../../../components/ui/Alert';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';
import { GrowerStatusBadge } from '../components/GrowerStatusBadge';

export const GrowerProfilePage: FC = () => {
  const { profile, isLoading, isError, updateProfile, isUpdating } = useGrowerProfile();
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [formState, setFormState] = useState<{
    businessName?: string;
    contactName?: string;
    phone?: string;
    address?: string;
    city?: string;
    state?: string;
    zipCode?: string;
    bio?: string;
  }>({});

  if (isLoading) {
    return <GrowerSkeleton type="card" count={2} />;
  }

  if (isError || !profile) {
    return <GrowerErrorState />;
  }

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateProfile(formState);
      setSuccessMsg('Grower profile updated successfully.');
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch {
      // Handled by hook error
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', maxWidth: '800px' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
          Grower Business Profile
        </h1>
        <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
          Manage your grower identity, certification, and business contact details.
        </p>
      </div>

      {successMsg && <Alert variant="success">{successMsg}</Alert>}

      <Card style={{ padding: '1.5rem', backgroundColor: '#0d231a', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div>
            <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 600 }}>ACCOUNT IDENTITY</span>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 600, color: '#f3f4f6', margin: 0 }}>{profile.businessName}</h2>
          </div>
          <GrowerStatusBadge label={profile.status} variant={profile.status === 'ACTIVE' ? 'success' : 'warning'} />
        </div>

        <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', color: '#e5e7eb' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
                Business Name
              </label>
              <Input
                defaultValue={profile.businessName}
                onChange={(e) => setFormState((prev) => ({ ...prev, businessName: e.target.value }))}
                required
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
                Primary Contact Person
              </label>
              <Input
                defaultValue={profile.contactName}
                onChange={(e) => setFormState((prev) => ({ ...prev, contactName: e.target.value }))}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
                Email Address
              </label>
              <Input value={profile.email} disabled style={{ backgroundColor: 'rgba(255,255,255,0.05)', color: '#9ca3af' }} />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
                Phone Number
              </label>
              <Input
                defaultValue={profile.phone}
                onChange={(e) => setFormState((prev) => ({ ...prev, phone: e.target.value }))}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
              Bio / Cultivation Specialization
            </label>
            <Textarea
              rows={3}
              defaultValue={profile.bio}
              onChange={(e) => setFormState((prev) => ({ ...prev, bio: e.target.value }))}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <Button type="submit" variant="primary" isLoading={isUpdating}>
              Save Profile Changes
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
