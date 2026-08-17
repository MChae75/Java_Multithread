import React from 'react';
import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import UserPortal from './pages/UserPortal';
import AdminPortal from './pages/AdminPortal';

function App() {
  return (
    <Router>
      <nav className="nav-bar">
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div className="nav-brand">
            woot!
          </div>
          <div style={{ color: 'var(--white)', fontSize: '0.85rem', fontFamily: 'Open Sans', opacity: 0.8 }}>
            (a monkey-run enterprise)
          </div>
        </div>
        <div className="nav-links">
          <NavLink to="/" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'} end>
            Daily Deals
          </NavLink>
          <NavLink to="/admin" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Admin Stuff
          </NavLink>
        </div>
      </nav>

      <Routes>
        <Route path="/" element={<UserPortal />} />
        <Route path="/admin" element={<AdminPortal />} />
      </Routes>
    </Router>
  );
}

export default App;
