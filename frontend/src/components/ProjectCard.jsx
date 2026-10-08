import { Link } from 'react-router-dom'
import RoleBadge from './RoleBadge.jsx'

export default function ProjectCard({ project, onDelete }) {
  const progress = project.totalTasks > 0
    ? Math.round((project.doneTasks / project.totalTasks) * 100)
    : 0

  return (
    <div className="card project-card">
      <div className="project-card-header">
        <Link to={`/projects/${project.id}`} className="project-title">{project.name}</Link>
        {project.myRole === 'OWNER' && (
          <button className="btn btn-danger btn-sm" onClick={() => onDelete(project.id)}>Delete</button>
        )}
      </div>
      <div style={{ margin: '6px 0' }}>
        <RoleBadge role={project.myRole} />
        {project.myRole !== 'OWNER' && (
          <span className="readonly-note"> &middot; owned by {project.ownerUsername}</span>
        )}
      </div>
      {project.description && <p className="project-desc">{project.description}</p>}
      <div className="progress-bar">
        <div className="progress-fill" style={{ width: `${progress}%` }} />
      </div>
      <div className="project-meta">
        {project.doneTasks}/{project.totalTasks} tasks done ({progress}%) &middot; {project.memberCount} member{project.memberCount !== 1 ? 's' : ''}
      </div>
    </div>
  )
}
