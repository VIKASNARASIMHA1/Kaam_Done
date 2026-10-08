import { useEffect, useState } from 'react'
import api from '../api/axios'

export default function ActivityFeed({ projectId, refreshKey }) {
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const { data } = await api.get(`/projects/${projectId}/activity`)
        setEntries(data)
      } catch (err) {
        setError('Failed to load activity.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [projectId, refreshKey])

  if (loading) return <p>Loading activity...</p>
  if (error) return <div className="alert-error">{error}</div>

  return (
    <div className="card">
      {entries.length === 0 ? (
        <p className="readonly-note">No activity yet.</p>
      ) : (
        <div className="activity-list">
          {entries.map((entry) => (
            <div className="activity-item" key={entry.id}>
              <div className="activity-dot" />
              <div>
                <div className="activity-text">{entry.description}</div>
                <div className="activity-time">{new Date(entry.createdAt).toLocaleString()}</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
