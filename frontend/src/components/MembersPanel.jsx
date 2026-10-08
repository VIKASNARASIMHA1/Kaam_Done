import { useEffect, useState } from 'react'
import api from '../api/axios'
import RoleBadge from './RoleBadge.jsx'

function avatarColor(name) {
  let hash = 0
  for (let i = 0; i < (name?.length || 0); i++) hash = (hash * 31 + name.charCodeAt(i)) % 360
  return `hsl(${hash}, 45%, 42%)`
}

export default function MembersPanel({ projectId, myRole }) {
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [username, setUsername] = useState('')
  const [role, setRole] = useState('EDITOR')

  const isOwner = myRole === 'OWNER'

  const load = async () => {
    setLoading(true)
    try {
      const { data } = await api.get(`/projects/${projectId}/members`)
      setMembers(data)
    } catch (err) {
      setError('Failed to load members.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId])

  const handleAdd = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await api.post(`/projects/${projectId}/members`, { username, role })
      setUsername('')
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add member.')
    }
  }

  const handleRoleChange = async (memberId, newRole) => {
    try {
      await api.put(`/projects/${projectId}/members/${memberId}`, { role: newRole })
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update role.')
    }
  }

  const handleRemove = async (memberId, name) => {
    if (!window.confirm(`Remove ${name} from this project?`)) return
    try {
      await api.delete(`/projects/${projectId}/members/${memberId}`)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove member.')
    }
  }

  if (loading) return <p>Loading members...</p>

  return (
    <div className="card">
      {error && <div className="alert-error">{error}</div>}
      {members.map((m) => (
        <div className="member-row" key={m.id}>
          <div className="member-info">
            <div className="member-avatar" style={{ background: avatarColor(m.username) }}>{m.username?.[0]}</div>
            <div>
              <div>{m.username}</div>
              <div className="readonly-note">{m.email}</div>
            </div>
          </div>
          <div className="member-actions">
            {isOwner && m.role !== 'OWNER' ? (
              <select value={m.role} onChange={(e) => handleRoleChange(m.id, e.target.value)}>
                <option value="EDITOR">Editor</option>
                <option value="VIEWER">Viewer</option>
              </select>
            ) : (
              <RoleBadge role={m.role} />
            )}
            {isOwner && m.role !== 'OWNER' && (
              <button className="btn btn-danger btn-sm" onClick={() => handleRemove(m.id, m.username)}>Remove</button>
            )}
          </div>
        </div>
      ))}

      {isOwner ? (
        <form className="add-member-form" onSubmit={handleAdd}>
          <input
            placeholder="Username to invite"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="EDITOR">Editor</option>
            <option value="VIEWER">Viewer</option>
          </select>
          <button type="submit" className="btn btn-primary">Add</button>
        </form>
      ) : (
        <p className="readonly-note">Only the project owner can manage members.</p>
      )}
    </div>
  )
}
