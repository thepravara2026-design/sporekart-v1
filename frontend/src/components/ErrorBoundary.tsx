import { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
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
    console.error('Uncaught error in React Component tree:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="container" style={{ textAlign: 'center', paddingTop: '4rem' }}>
          <div className="card" style={{ maxWidth: '600px', margin: '0 auto' }}>
            <h2 style={{ color: 'var(--danger-color)', marginBottom: '1rem' }}>Application Rendering Exception</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              An unexpected error occurred in the user interface.
            </p>
            <button
              className="btn btn-primary"
              onClick={() => window.location.reload()}
            >
              Reload Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
