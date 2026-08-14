import { FC, useEffect, useState } from 'react';
import { apiClient } from '../services/apiClient';
import { HealthStatusData, VersionInfoData } from '../types/api';

export const HealthPage: FC = () => {
  const [health, setHealth] = useState<HealthStatusData | null>(null);
  const [version, setVersion] = useState<VersionInfoData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchHealthAndVersion = () => {
    setLoading(true);
    setErrorMessage(null);
    apiClient.getHealth()
      .then((healthRes) => {
        setHealth(healthRes.data);
        return apiClient.getVersion();
      })
      .then((versionRes) => {
        setVersion(versionRes.data);
      })
      .catch((err: unknown) => {
        if (err instanceof Error) {
          setErrorMessage(err.message);
        } else {
          setErrorMessage('Backend service is currently unavailable.');
        }
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    let isMounted = true;
    apiClient.getHealth()
      .then((healthRes) => {
        if (isMounted) setHealth(healthRes.data);
        return apiClient.getVersion();
      })
      .then((versionRes) => {
        if (isMounted) setVersion(versionRes.data);
      })
      .catch((err: unknown) => {
        if (isMounted) {
          if (err instanceof Error) {
            setErrorMessage(err.message);
          } else {
            setErrorMessage('Backend service is currently unavailable.');
          }
        }
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <div style={{ maxWidth: '700px', margin: '0 auto' }}>
      <h1 style={{ marginBottom: '1rem', fontSize: '2rem' }}>System Operational Health</h1>
      
      <div className="card">
        <h2 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Backend Service Status</h2>
        
        {loading && (
          <p style={{ color: 'var(--text-secondary)' }}>Querying system health endpoints...</p>
        )}

        {!loading && health && (
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
              <span className="badge badge-success">OPERATIONAL</span>
              <span style={{ fontSize: '1.1rem', fontWeight: 600 }}>Status: {health.status}</span>
            </div>
            
            {version && (
              <div style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <p style={{ margin: '0.25rem 0' }}><strong>Application Name:</strong> {version.appName}</p>
                <p style={{ margin: '0.25rem 0' }}><strong>Build Version:</strong> {version.version}</p>
              </div>
            )}
          </div>
        )}

        {!loading && errorMessage && (
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
              <span className="badge badge-danger">UNAVAILABLE</span>
              <span style={{ color: 'var(--danger-color)', fontWeight: 600 }}>Service Offline</span>
            </div>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              {errorMessage}
            </p>
          </div>
        )}

        <div style={{ marginTop: '1.5rem' }}>
          <button className="btn btn-primary" onClick={fetchHealthAndVersion} disabled={loading}>
            {loading ? 'Checking...' : 'Refresh Status'}
          </button>
        </div>
      </div>
    </div>
  );
};
