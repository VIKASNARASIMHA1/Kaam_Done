import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DndContext, PointerSensor, useSensor, useSensors } from '@dnd-kit/core'
import api from '../api/axios'
import KanbanColumn from '../components/KanbanColumn.jsx'
import TaskModal from '../components/TaskModal.jsx'
import TaskDetailModal from '../components/TaskDetailModal.jsx'
import MembersPanel from '../components/MembersPanel.jsx'
import ActivityFeed from '../components/ActivityFeed.jsx'
import RoleBadge from '../components/RoleBadge.jsx'

const columns = [
  { key: 'TODO', label: 'To Do' },
  { key: 'IN_PROGRESS', label: 'In Progress' },
  { key: 'DONE', label: 'Done' }
]

export default function ProjectDetail() {
  const { projectId } = useParams()
  const [project, setProject] = useState(null)
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [tab, setTab] = useState('board')
  const [createOpen, setCreateOpen] = useState(false)
  const [activeTask, setActiveTask] = useState(null)
  const [activityRefresh, setActivityRefresh] = useState(0)

  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 8 } }))

  const canEdit = project?.myRole === 'OWNER' || project?.myRole === 'EDITOR'

  const loadData = async () => {
    setLoading(true)
    try {
      const [projectRes, tasksRes] = await Promise.all([
        api.get(`/projects/${projectId}`),
        api.get(`/projects/${projectId}/tasks`)
      ])
      setProject(projectRes.data)
      setTasks(tasksRes.data)
    } catch (err) {
      setError('Failed to load project.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId])

  const handleCreateSave = async (form) => {
    try {
      await api.post(`/projects/${projectId}/tasks`, form)
      setCreateOpen(false)
      loadData()
      setActivityRefresh((n) => n + 1)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create task.')
    }
  }

  const handleTaskUpdated = (updatedTask) => {
    setTasks((prev) => prev.map((t) => (t.id === updatedTask.id ? updatedTask : t)))
    setActiveTask(null)
    setActivityRefresh((n) => n + 1)
  }

  const handleTaskDeleted = (taskId) => {
    setTasks((prev) => prev.filter((t) => t.id !== taskId))
    setActiveTask(null)
    setActivityRefresh((n) => n + 1)
  }

  const handleDragEnd = async (event) => {
    const { active, over } = event
    if (!over || !canEdit) return

    const task = active.data.current?.task
    const newStatus = over.id
    if (!task || task.status === newStatus) return

    // optimistic update
    setTasks((prev) => prev.map((t) => (t.id === task.id ? { ...t, status: newStatus } : t)))

    try {
      await api.put(`/projects/${projectId}/tasks/${task.id}`, {
        title: task.title,
        description: task.description,
        status: newStatus,
        priority: task.priority,
        dueDate: task.dueDate
      })
      setActivityRefresh((n) => n + 1)
    } catch (err) {
      setError('Failed to move task.')
      loadData()
    }
  }

  if (loading) return <div className="page"><p>Loading...</p></div>

  return (
    <div className="page">
      <Link to="/" className="back-link">&larr; Back to projects</Link>
      <div className="page-header">
        <div>
          <h1>{project?.name} <RoleBadge role={project?.myRole} /></h1>
          {project?.description && <p className="subtitle">{project.description}</p>}
        </div>
        {tab === 'board' && canEdit && (
          <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>+ New Task</button>
        )}
      </div>

      {error && <div className="alert-error">{error}</div>}

      <div className="tabs">
        <button className={`tab-btn ${tab === 'board' ? 'active' : ''}`} onClick={() => setTab('board')}>Board</button>
        <button className={`tab-btn ${tab === 'activity' ? 'active' : ''}`} onClick={() => setTab('activity')}>Activity</button>
        <button className={`tab-btn ${tab === 'members' ? 'active' : ''}`} onClick={() => setTab('members')}>
          Members ({project?.memberCount})
        </button>
      </div>

      {!canEdit && tab === 'board' && (
        <p className="readonly-note" style={{ marginBottom: 12 }}>You have view-only access to this project.</p>
      )}

      {tab === 'board' && (
        <DndContext sensors={sensors} onDragEnd={handleDragEnd}>
          <div className="kanban">
            {columns.map((col) => (
              <KanbanColumn
                key={col.key}
                id={col.key}
                label={col.label}
                tasks={tasks.filter((t) => t.status === col.key)}
                onOpenTask={setActiveTask}
              />
            ))}
          </div>
        </DndContext>
      )}

      {tab === 'activity' && <ActivityFeed projectId={projectId} refreshKey={activityRefresh} />}

      {tab === 'members' && <MembersPanel projectId={projectId} myRole={project?.myRole} />}

      <TaskModal
        isOpen={createOpen}
        onClose={() => setCreateOpen(false)}
        onSave={handleCreateSave}
        initialData={null}
      />

      <TaskDetailModal
        isOpen={!!activeTask}
        onClose={() => setActiveTask(null)}
        task={activeTask}
        projectId={projectId}
        myRole={project?.myRole}
        onUpdated={handleTaskUpdated}
        onDeleted={handleTaskDeleted}
      />
    </div>
  )
}
