import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardHeader, CardTitle, CardContent } from '../../../components/ui/Card';
import { Shield, RotateCcw, BookOpen, Layers, Activity, Bell, FileText, CheckCircle2 } from 'lucide-react';

export const AdminDashboardPage: FC = () => {
  const adminModules = [
    { title: 'Returns Audit', desc: 'Inspect customer return requests & refund authorizations', icon: RotateCcw, path: '/admin/returns', count: '14 Active Requests' },
    { title: 'Training Programs', desc: 'Manage curriculum, course titles & guidelines', icon: BookOpen, path: '/admin/training', count: '8 Programs' },
    { title: 'Batch Management', desc: 'Schedule capacity, physical locations & dates', icon: Layers, path: '/admin/training/batches', count: '24 Batches' },
    { title: 'Training Operations', desc: 'Manage enrollment status & exception queue', icon: Activity, path: '/admin/training/operations', count: '100% Operational' },
    { title: 'Training Reports', desc: 'Attendance, certificates & audit logs', icon: FileText, path: '/admin/training/reports', count: 'Audit Log Ready' },
    { title: 'Notification Ops', desc: 'Monitor outbox dispatch, providers & SMS logs', icon: Bell, path: '/admin/notifications', count: 'Providers Healthy' },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Admin Control Panel</h1>
          <p style={{ color: '#64748b', margin: '4px 0 0', fontSize: '0.875rem' }}>
            System-wide operational management and oversight across commerce, training, and notifications
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 16px', background: '#f0fdf4', color: '#166534', borderRadius: '8px', fontSize: '0.875rem', fontWeight: 600 }}>
          <CheckCircle2 size={18} />
          System Operational
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.25rem', marginTop: '0.5rem' }}>
        {adminModules.map(mod => {
          const Icon = mod.icon;
          return (
            <Link key={mod.path} to={mod.path} style={{ textDecoration: 'none' }}>
              <Card className="admin-module-card" style={{ height: '100%', transition: 'transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease', cursor: 'pointer', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(15, 23, 42, 0.08)' }}>
                <CardHeader>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ padding: '10px', background: '#f1f5f9', borderRadius: '10px', color: '#0f172a', transition: 'background-color 0.2s ease, color 0.2s ease' }}>
                      <Icon size={24} />
                    </div>
                    <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#64748b', background: '#f8fafc', padding: '4px 8px', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
                      {mod.count}
                    </span>
                  </div>
                  <CardTitle style={{ marginTop: '1rem', fontSize: '1.125rem' }}>{mod.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p style={{ fontSize: '0.875rem', color: '#64748b', margin: 0 }}>{mod.desc}</p>
                </CardContent>
              </Card>
            </Link>
          );
        })}
      </div>

      <Card style={{ marginTop: '1rem' }}>
        <CardHeader>
          <CardTitle style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1rem' }}>
            <Shield size={18} style={{ color: '#dc2626' }} />
            System Authority Matrix
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div style={{ fontSize: '0.875rem', color: '#475569', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
            <div style={{ padding: '12px', background: '#f8fafc', borderRadius: '6px' }}>
              <div style={{ fontWeight: 600, color: '#0f172a' }}>Role: ROLE_ADMIN</div>
              <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '4px' }}>Full system administration</div>
            </div>
            <div style={{ padding: '12px', background: '#f8fafc', borderRadius: '6px' }}>
              <div style={{ fontWeight: 600, color: '#0f172a' }}>Role: ROLE_GROWER</div>
              <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '4px' }}>Grower portal & inventory management</div>
            </div>
            <div style={{ padding: '12px', background: '#f8fafc', borderRadius: '6px' }}>
              <div style={{ fontWeight: 600, color: '#0f172a' }}>Role: ROLE_TRAINEE</div>
              <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '4px' }}>Academy courses & enrollment</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
