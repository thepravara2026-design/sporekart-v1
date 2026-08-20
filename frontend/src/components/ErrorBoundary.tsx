import { Component, ErrorInfo, ReactNode } from 'react';
import { AlertOctagon, RefreshCw } from 'lucide-react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    if (import.meta.env.DEV) {
      console.error('Uncaught exception caught in React Error Boundary:', error, errorInfo);
    }
  }

  private handleReset = () => {
    this.setState({ hasError: false, error: undefined });
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div
          role="alert"
          aria-live="assertive"
          className="container"
          style={{ textAlign: 'center', paddingTop: '4rem', paddingBottom: '4rem' }}
        >
          <div className="card" style={{ maxWidth: '540px', margin: '0 auto', padding: '2.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
              <AlertOctagon size={48} style={{ color: 'var(--danger-color)' }} />
            </div>
            <h2 style={{ color: 'var(--text-primary)', marginBottom: '0.75rem', fontSize: '1.5rem', fontWeight: 800 }}>
              Application Execution Exception
            </h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', lineHeight: 1.6, fontSize: '0.95rem' }}>
              An unexpected user-interface exception occurred. Diagnostic details have been recorded safely.
            </p>
            <button
              className="btn btn-primary"
              onClick={this.handleReset}
              aria-label="Reload and reset application"
            >
              <RefreshCw size={16} /> Reload Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
