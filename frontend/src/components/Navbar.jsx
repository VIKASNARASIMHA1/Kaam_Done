import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useTheme } from '../context/ThemeContext.jsx'
import Logo from './Logo.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand"><Logo size={28} />Kaam Done</Link>
      <div className="navbar-right">
        <button className="btn btn-ghost theme-toggle" onClick={toggleTheme} title="Toggle dark mode">
          {theme === 'light' ? '🌙' : '☀️'}
        </button>
        <span className="navbar-avatar" title={user?.username}>{user?.username?.[0]?.toUpperCase()}</span>
        <span className="navbar-user">{user?.username}</span>
        <button className="btn btn-ghost" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  )
}
