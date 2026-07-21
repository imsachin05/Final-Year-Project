import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Sidebar() {
  const { username, logout } = useAuth()

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark" />
        <div className="brand-name">Final Year Project</div>
      </div>

      <NavLink to="/" end className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
        Dashboard
      </NavLink>
      <NavLink to="/trades" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
        Trades
      </NavLink>
      <NavLink to="/trades/new" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
        Add Trade
      </NavLink>
      <NavLink to="/ai" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
        AI Dashboard
      </NavLink>
      <NavLink to="/assistant" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
        Ask Your Journal
      </NavLink>

      <div className="sidebar-footer">
        <div style={{ fontSize: 13, marginBottom: 8, color: 'var(--text-muted)' }}>
          Signed in as <strong style={{ color: 'var(--text-primary)' }}>{username}</strong>
        </div>
        <button className="logout-btn" onClick={logout}>Log out</button>
      </div>
    </aside>
  )
}
