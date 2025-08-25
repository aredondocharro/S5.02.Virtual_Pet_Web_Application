import { Link } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { createPortal } from 'react-dom';
import { useAuth } from '../context/AuthContext';
import './Home.css';

type ThemeKey = 'day' | 'tropical' | 'night';

export default function Home() {
  const { user, loadingUser, logout } = useAuth();
  const [theme, setTheme] = useState<ThemeKey>(() => {
    return (localStorage.getItem('theme') as ThemeKey) || 'night';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const username = !loadingUser ? (user?.username ?? '') : '';
  const email = !loadingUser ? (user?.email ?? '') : '';
  const avatarUrl = user?.avatarUrl ?? '';
  const profileLoading = loadingUser;

  const FxLayer = useMemo(() => {
    switch (theme) {
      case 'day':      return <VortexFX />;   // torbellino
      case 'tropical': return <GlowFX />;     // glow
      case 'night':
      default:         return <StarsFX />;    // estrellas
    }
  }, [theme]);

  return (
    <div className="home">
      {/* BG */}
      <div className="home__bg" aria-hidden />

      {/* FX portalizados a <body> (evita stacking context del layout) */}
      {createPortal(FxLayer, document.body)}

      {/* Header */}
      <header className="home__header">
        <div className="home__header-inner">
          <div className="brand">
            <div className="brand__badge" aria-hidden>🦎</div>
            <span className="brand__name">AXOLOTL MANAGER</span>
          </div>

          <div className="header-right">
            <div className="theme-switch" role="group" aria-label="Theme selector">
              <button className={`chip ${theme === 'day' ? 'active' : ''}`} onClick={() => setTheme('day')} title="Day (vortex)">
                <span aria-hidden>☀️</span><span className="chip__label">Day</span>
              </button>
              <button className={`chip ${theme === 'tropical' ? 'active' : ''}`} onClick={() => setTheme('tropical')} title="Tropical (glow)">
                <span aria-hidden>🏝️</span><span className="chip__label">Tropical</span>
              </button>
              <button className={`chip ${theme === 'night' ? 'active' : ''}`} onClick={() => setTheme('night')} title="Night (starfall)">
                <span aria-hidden>🌙</span><span className="chip__label">Night</span>
              </button>
            </div>

            <Link to="/app/profile" className={`profile ${profileLoading ? 'is-loading' : ''}`} aria-label="Profile">
              <div className="profile__text">
                <span className="profile__name">{username}</span>
                <span className="profile__email">{email}</span>
              </div>
              <div className="profile__avatar" aria-hidden>
                {avatarUrl ? <img src={avatarUrl} alt="" /> : <span className="avatar-fallback">{(username?.[0] ?? 'U').toUpperCase()}</span>}
              </div>
            </Link>

            <button className="btn btn-ghost" onClick={logout} title="Logout">Logout</button>
          </div>
        </div>
      </header>

      {/* Main */}
      <main className="home__main">
        <section className="hero hero--image">
          <figure className="hero-banner">
            <img src="/banners/home-banner.png" alt="Axolotl banner" loading="eager" decoding="async" />
          </figure>

          <div className="hero__actions">
            <Link to="/app/pets/new" className="btn btn-primary">
              <span className="btn__icon" aria-hidden>+</span>
              Create Pet
            </Link>

            <Link to="/app/sanctuary" className="btn btn-secondary">
              <span className="btn__icon" aria-hidden>🏠</span>
              Sanctuary
            </Link>
          </div>
        </section>

        <section className="features">
          <article className="feature">
            <div className="feature__icon">+</div>
            <h3 className="feature__title">Add New Pets</h3>
            <p className="feature__desc">Create detailed profiles with names, colors, and unique traits.</p>
          </article>

          <article className="feature">
            <div className="feature__icon">🏠</div>
            <h3 className="feature__title">Visit Sanctuary</h3>
            <p className="feature__desc">See all your virtual pets together, happy and thriving.</p>
          </article>

          <article className="feature">
            <div className="feature__icon">👤</div>
            <h3 className="feature__title">Manage Profile</h3>
            <p className="feature__desc">Update your info and personalize the experience.</p>
          </article>
        </section>
      </main>
    </div>
  );
}

/* ==== FX components ==== */
function StarsFX() {
  // Sin delays para que se vean ya
  const stars = Array.from({ length: 48 });
  return (
    <div className="fx fx--stars" aria-hidden>
      {stars.map((_, i) => (
        <span
          key={i}
          className="fx-star"
          style={{
            left: `${(i * 6.8) % 100}%`,
            // forzamos sin delay para visibilidad inmediata
            animationDelay: '0s',
            animationDuration: `${7 + (i % 5)}s`,
          }}
        />
      ))}
    </div>
  );
}

function GlowFX() {
  // Glow contenido dentro para controlar z-index sin tapar header
  return (
    <div className="fx fx--glow" aria-hidden>
      <div className="fx-glow-blob" />
    </div>
  );
}

function VortexFX() {
  // Más partículas y opacidad desde el inicio para asegurarlo
  const particles = Array.from({ length: 64 });
  return (
    <div className="fx fx--vortex" aria-hidden>
      <div className="vortex-haze" />
      {particles.map((_, i) => {
        const duration = 6 + (i % 4);        // 6..9s
        const startRadius = 40 - (i % 8) * 3; // vmin
        const size = 4 + (i % 3);             // 4..6px
        return (
          <span
            key={i}
            className="fx-vortex-particle"
            style={
              {
                '--delay': `0s`,                         // sin delay
                '--dur': `${duration}s`,
                '--r-start': `${startRadius}vmin`,
                '--size': `${size}px`,
              } as React.CSSProperties
            }
          />
        );
      })}
    </div>
  );
}
