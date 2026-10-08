import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import AuthLayout, { PasswordField } from '../components/AuthLayout.jsx'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check your credentials.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Log in to pick up where you left off."
      onSubmit={handleSubmit}
      error={error}
      footer={<>New here? <Link to="/register">Create an account</Link></>}
    >
      <label>Username</label>
      <input value={username} onChange={(e) => setUsername(e.target.value)} required autoFocus autoComplete="username" placeholder="your username" />
      <PasswordField value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Enter your password" />
      <button className="btn btn-primary btn-block" disabled={loading}>
        {loading ? 'Logging in...' : 'Log in'}
      </button>
    </AuthLayout>
  )
}
