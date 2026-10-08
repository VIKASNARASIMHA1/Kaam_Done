import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
  PieChart, Pie, Cell, Legend
} from 'recharts'

const COLORS = ['#1f6f65', '#e1e3dc']

export default function DashboardAnalytics({ projects }) {
  if (projects.length === 0) return null

  const barData = projects.slice(0, 8).map((p) => ({
    name: p.name.length > 12 ? p.name.slice(0, 12) + '…' : p.name,
    done: p.doneTasks,
    remaining: Math.max(p.totalTasks - p.doneTasks, 0)
  }))

  const totalDone = projects.reduce((sum, p) => sum + p.doneTasks, 0)
  const totalTasks = projects.reduce((sum, p) => sum + p.totalTasks, 0)
  const totalRemaining = Math.max(totalTasks - totalDone, 0)
  const pieData = [
    { name: 'Completed', value: totalDone },
    { name: 'Remaining', value: totalRemaining }
  ]

  return (
    <div className="analytics-grid">
      <div className="card analytics-card">
        <h3>Tasks per Project</h3>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={barData}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
            <XAxis dataKey="name" fontSize={12} stroke="var(--text-muted)" />
            <YAxis fontSize={12} allowDecimals={false} stroke="var(--text-muted)" />
            <Tooltip />
            <Bar dataKey="done" stackId="a" fill="#1f6f65" name="Done" />
            <Bar dataKey="remaining" stackId="a" fill="#c08a2e" name="Remaining" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="card analytics-card">
        <h3>Overall Completion</h3>
        {totalTasks === 0 ? (
          <p className="readonly-note">No tasks yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={pieData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={2}>
                {pieData.map((entry, index) => (
                  <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  )
}
