import { FC } from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from '../../../components/layout/Header';
import { Footer } from '../../../components/layout/Footer';
import { SkipLink } from '../../../components/layout/SkipLink';
import { GraduationCap, Award, BookOpen, Clock } from 'lucide-react';

export const TrainingLayout: FC = () => {

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', background: '#f8fafc' }}>
      <SkipLink targetId="training-main-content" label="Skip to training content" />
      <Header />

      <div style={{ background: '#0f172a', color: '#ffffff', padding: '1.5rem 0', borderBottom: '1px solid #1e293b' }}>
        <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '0 1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ padding: '10px', background: '#2563eb', borderRadius: '10px' }}>
              <GraduationCap size={24} />
            </div>
            <div>
              <h1 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>Sporekart Mushroom Academy</h1>
              <p style={{ fontSize: '0.8125rem', color: '#94a3b8', margin: '2px 0 0' }}>
                Professional Mycology Training, Substrate Sterilization & Cultivation Workshops
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '16px', alignItems: 'center', fontSize: '0.8125rem', color: '#cbd5e1' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Award size={16} style={{ color: '#fbbf24' }} /> Certified Curriculum
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <BookOpen size={16} style={{ color: '#38bdf8' }} /> Hands-on Labs
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Clock size={16} style={{ color: '#4ade80' }} /> Verified Schedule
            </div>
          </div>
        </div>
      </div>

      <main id="training-main-content" tabIndex={-1} style={{ flex: 1, padding: '2rem 1.5rem', maxWidth: '1280px', width: '100%', margin: '0 auto', outline: 'none' }}>
        <Outlet />
      </main>

      <Footer />
    </div>
  );
};
