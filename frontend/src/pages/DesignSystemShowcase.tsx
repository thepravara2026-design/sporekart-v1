import { FC, useState } from 'react';
import {
  primitiveColors,
  semanticColors,
  fontFamilies,
  fontSizes,
  radii,
  shadows,
  spacing,
} from '../design-system/tokens';
import {
  CheckCircle,
  AlertTriangle,
  XCircle,
  Info,
  Sparkles,
  ArrowRight,
  Shield,
  Layers,
  Palette,
  Type,
} from 'lucide-react';

export const DesignSystemShowcase: FC = () => {
  const [activeTab, setActiveTab] = useState<'colors' | 'typography' | 'primitives' | 'states'>('colors');

  return (
    <div style={{ padding: '2rem 1.5rem', maxWidth: '1200px', margin: '0 auto' }}>
      <header style={{ marginBottom: '2.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
          <Sparkles style={{ color: primitiveColors.forest[500], width: '28px', height: '28px' }} />
          <h1 style={{ fontSize: '2.25rem', fontWeight: 800, color: semanticColors.text.primary }}>
            Sporekart Design System Foundation
          </h1>
        </div>
        <p style={{ color: semanticColors.text.secondary, fontSize: '1rem' }}>
          Centralized Visual Design Tokens & Component Validation Surface (Sprint FD-03)
        </p>
      </header>

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '2rem', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '0.75rem' }}>
        <button
          onClick={() => setActiveTab('colors')}
          className={`btn ${activeTab === 'colors' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Palette size={16} /> Color Tokens
        </button>
        <button
          onClick={() => setActiveTab('typography')}
          className={`btn ${activeTab === 'typography' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Type size={16} /> Typography & Fonts
        </button>
        <button
          onClick={() => setActiveTab('primitives')}
          className={`btn ${activeTab === 'primitives' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Layers size={16} /> Primitive Components
        </button>
        <button
          onClick={() => setActiveTab('states')}
          className={`btn ${activeTab === 'states' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Shield size={16} /> Interaction & Status States
        </button>
      </div>

      {/* TAB 1: COLORS */}
      {activeTab === 'colors' && (
        <section className="card">
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1rem', color: semanticColors.text.primary }}>
            Sporekart Forest Color Palette
          </h2>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
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

          <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem', color: semanticColors.text.primary }}>
            Semantic Tokens
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1rem' }}>
            <div style={{ background: semanticColors.background.primary, padding: '1rem', border: '1px solid rgba(255,255,255,0.1)', borderRadius: radii.md }}>
              <div style={{ fontWeight: 600 }}>Background Primary</div>
              <div style={{ fontSize: '0.875rem', color: semanticColors.text.secondary }}>#051c14</div>
            </div>
            <div style={{ background: semanticColors.background.surface, padding: '1rem', border: '1px solid rgba(255,255,255,0.1)', borderRadius: radii.md }}>
              <div style={{ fontWeight: 600 }}>Background Surface</div>
              <div style={{ fontSize: '0.875rem', color: semanticColors.text.secondary }}>#0a291d</div>
            </div>
            <div style={{ background: semanticColors.background.card, padding: '1rem', border: '1px solid rgba(255,255,255,0.1)', borderRadius: radii.md }}>
              <div style={{ fontWeight: 600 }}>Glassmorphic Card</div>
              <div style={{ fontSize: '0.875rem', color: semanticColors.text.secondary }}>rgba(19, 66, 51, 0.75)</div>
            </div>
          </div>
        </section>
      )}

      {/* TAB 2: TYPOGRAPHY */}
      {activeTab === 'typography' && (
        <section className="card">
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem', color: semanticColors.text.primary }}>
            Typography Scale & Regional Locale Support
          </h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div>
              <span style={{ fontSize: '0.75rem', color: semanticColors.text.muted, textTransform: 'uppercase' }}>Heading 1 (2.25rem / 36px)</span>
              <h1 style={{ fontSize: fontSizes['4xl'][0] as string, fontWeight: 800 }}>Sporekart Mushroom Cultivation</h1>
            </div>
            <div>
              <span style={{ fontSize: '0.75rem', color: semanticColors.text.muted, textTransform: 'uppercase' }}>Kannada Locale Support (Noto Sans Kannada)</span>
              <h2 style={{ fontFamily: fontFamilies.kannada, fontSize: fontSizes['2xl'][0] as string, fontWeight: 700, color: primitiveColors.forest[400] }}>
                ಸ್ಪೋರ್‌ಕಾರ್ಟ್ ಅಣಬೆ ಕೃಷಿ ತರಬೇತಿ ಮತ್ತು ಮಾರುಕಟ್ಟೆ
              </h2>
            </div>
            <div>
              <span style={{ fontSize: '0.75rem', color: semanticColors.text.muted, textTransform: 'uppercase' }}>Body Medium (1rem / 16px)</span>
              <p style={{ fontSize: fontSizes.base[0] as string, color: semanticColors.text.secondary }}>
                High-yielding oyster and button mushroom spawn batches with temperature-controlled shipping and automated grower support.
              </p>
            </div>
          </div>
        </section>
      )}

      {/* TAB 3: PRIMITIVES */}
      {activeTab === 'primitives' && (
        <section className="card">
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem', color: semanticColors.text.primary }}>
            Buttons, Badges & Spacing Scale
          </h2>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: spacing[4], marginBottom: '2rem' }}>
            <button className="btn btn-primary">
              Primary Action <ArrowRight size={16} />
            </button>
            <button className="btn btn-secondary">Secondary Action</button>
            <button className="btn btn-primary" disabled>
              Disabled Action
            </button>
          </div>

          <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem', color: semanticColors.text.primary }}>
            Status Badges with Lucide Icons
          </h3>
          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
            <span className="badge badge-success">
              <CheckCircle size={14} /> Active / In Stock
            </span>
            <span className="badge badge-danger">
              <XCircle size={14} /> Out of Stock
            </span>
            <span className="badge" style={{ background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b', border: '1px solid rgba(245, 158, 11, 0.3)' }}>
              <AlertTriangle size={14} /> Low Inventory
            </span>
            <span className="badge" style={{ background: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.3)' }}>
              <Info size={14} /> System Info
            </span>
          </div>
        </section>
      )}

      {/* TAB 4: STATES */}
      {activeTab === 'states' && (
        <section className="card">
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem', color: semanticColors.text.primary }}>
            Keyboard Focus & Reduced Motion Verification
          </h2>
          <p style={{ color: semanticColors.text.secondary, marginBottom: '1.5rem' }}>
            Test keyboard focus rings by pressing Tab. Notice high-contrast 2px bio-emerald outline offset.
          </p>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <input
              type="text"
              placeholder="Tab focus target input..."
              style={{
                background: 'rgba(0,0,0,0.3)',
                border: '1px solid rgba(255,255,255,0.15)',
                borderRadius: radii.md,
                padding: '0.75rem 1rem',
                color: '#fff',
                width: '300px',
              }}
            />
            <button className="btn btn-primary">Focus Target Button</button>
          </div>
        </section>
      )}
    </div>
  );
};
