import { useDroppable } from '@dnd-kit/core'
import TaskCard from './TaskCard.jsx'

export default function KanbanColumn({ id, label, tasks, onOpenTask }) {
  const { setNodeRef, isOver } = useDroppable({ id })

  return (
    <div ref={setNodeRef} className={`kanban-column ${isOver ? 'drag-over' : ''}`}>
      <h3>{label} <span className="count-badge">{tasks.length}</span></h3>
      <div className="kanban-tasks">
        {tasks.map((task) => (
          <TaskCard key={task.id} task={task} onOpen={onOpenTask} />
        ))}
      </div>
    </div>
  )
}
