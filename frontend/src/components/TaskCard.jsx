import { useDraggable } from '@dnd-kit/core'

const priorityVar = { LOW: 'var(--priority-low)', MEDIUM: 'var(--priority-medium)', HIGH: 'var(--priority-high)' }

export default function TaskCard({ task, onOpen }) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: `task-${task.id}`,
    data: { task }
  })

  const style = {
    '--priority-color': priorityVar[task.priority],
    ...(transform
      ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`, zIndex: 10 }
      : {})
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      className={`card task-card ${isDragging ? 'dragging' : ''}`}
      onClick={() => onOpen(task)}
    >
      <div className="task-card-header">
        <h4>{task.title}</h4>
      </div>
      {task.description && <p className="task-desc">{task.description}</p>}
      <div className="task-meta">
        {task.dueDate && <span>Due {task.dueDate}</span>}
        {task.commentCount > 0 && <span> &middot; 💬 {task.commentCount}</span>}
        {task.attachmentCount > 0 && <span> &middot; 📎 {task.attachmentCount}</span>}
      </div>
    </div>
  )
}
