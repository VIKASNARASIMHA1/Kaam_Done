import { useEffect, useState } from 'react'
import api from '../api/axios'
import ProjectCard from '../components/ProjectCard.jsx'
import DashboardAnalytics from '../components/DashboardAnalytics.jsx'

export default function Dashboard() {
  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  const loadProjects = async () => {
    setLoading(true)
    try {
      const { data } = await api.get('/projects')
      setProjects(data)
    } catch (err) {
      setError('Failed to load projects.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProjects()
  }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    try {
      await api.post('/projects', { name, description })
      setName('')
      setDescription('')
      setShowForm(false)
      loadProjects()
    } catch (err) {
      setError('Failed to create project.')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this project and all its tasks?')) return
    try {
      await api.delete(`/projects/${id}`)
      setProjects((prev) => prev.filter((p) => p.id !== id))
    } catch (err) {
      setError('Failed to delete project.')
    }
  }

  const totalTasks = projects.reduce((sum, p) => sum + p.totalTasks, 0)
  const doneTasks = projects.reduce((sum, p) => sum + p.doneTasks, 0)

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Your Projects</h1>
          <p className="subtitle">
            {projects.length} project{projects.length !== 1 ? 's' : ''} &middot; {doneTasks}/{totalTasks} tasks completed
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm((s) => !s)}>
          {showForm ? 'Cancel' : '+ New Project'}
        </button>
      </div>

      {error && <div className="alert-error">{error}</div>}

      {!loading && projects.length > 0 && <DashboardAnalytics projects={projects} />}

      {showForm && (
        <form className="card inline-form" onSubmit={handleCreate}>
          <input
            placeholder="Project name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <input
            placeholder="Description (optional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Create</button>
        </form>
      )}

      {loading ? (
        <p>Loading...</p>
      ) : projects.length === 0 ? (
        <div className="empty-state">
          <p>No projects yet. Create your first one to get started!</p>
        </div>
      ) : (
        <div className="grid">
          {projects.map((project) => (
            <ProjectCard key={project.id} project={project} onDelete={handleDelete} />
          ))}
        </div>
      )}
    </div>
  )
}
