import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import AuthLayout, { PasswordField } from '../components/AuthLayout.jsx'

export default function Register() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(username, email, password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Set up your workspace in under a minute."
      onSubmit={handleSubmit}
      error={error}
      footer={<>Already have an account? <Link to="/login">Log in</Link></>}
    >
      <label>Username</label>
      <input value={username} onChange={(e) => setUsername(e.target.value)} required autoFocus minLength={3} autoComplete="username" placeholder="at least 3 characters" />
      <label>Email</label>
      <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" placeholder="you@example.com" />
      <PasswordField value={password} onChange={(e) => setPassword(e.target.value)} minLength={6} placeholder="at least 6 characters" />
      <button className="btn btn-primary btn-block" disabled={loading}>
        {loading ? 'Creating account...' : 'Create account'}
      </button>
    </AuthLayout>
  )
}
