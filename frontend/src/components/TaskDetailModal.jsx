import { useEffect, useRef, useState } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext.jsx'

function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export default function TaskDetailModal({ isOpen, onClose, task, projectId, myRole, onUpdated, onDeleted }) {
  const { user } = useAuth()
  const canEdit = myRole === 'OWNER' || myRole === 'EDITOR'

  const [form, setForm] = useState(null)
  const [comments, setComments] = useState([])
  const [newComment, setNewComment] = useState('')
  const [attachments, setAttachments] = useState([])
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const fileInputRef = useRef(null)

  useEffect(() => {
    if (!task || !isOpen) return
    setForm({
      title: task.title || '',
      description: task.description || '',
      status: task.status || 'TODO',
      priority: task.priority || 'MEDIUM',
      dueDate: task.dueDate || ''
    })
    setError('')
    loadComments()
    loadAttachments()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [task?.id, isOpen])

  const loadComments = async () => {
    try {
      const { data } = await api.get(`/projects/${projectId}/tasks/${task.id}/comments`)
      setComments(data)
    } catch (err) {
      // non-fatal
    }
  }

  const loadAttachments = async () => {
    try {
      const { data } = await api.get(`/projects/${projectId}/tasks/${task.id}/attachments`)
      setAttachments(data)
    } catch (err) {
      // non-fatal
    }
  }

  if (!isOpen || !task || !form) return null

  const handleSave = async (e) => {
    e.preventDefault()
    try {
      const { data } = await api.put(`/projects/${projectId}/tasks/${task.id}`, {
        ...form,
        dueDate: form.dueDate || null
      })
      onUpdated(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save task.')
    }
  }

  const handleDelete = async () => {
    if (!window.confirm(`Delete task "${task.title}"? This also deletes its comments and attachments.`)) return
    try {
      await api.delete(`/projects/${projectId}/tasks/${task.id}`)
      onDeleted(task.id)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete task.')
    }
  }

  const handleAddComment = async (e) => {
    e.preventDefault()
    if (!newComment.trim()) return
    try {
      await api.post(`/projects/${projectId}/tasks/${task.id}/comments`, { content: newComment })
      setNewComment('')
      loadComments()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add comment.')
    }
  }

  const handleDeleteComment = async (commentId) => {
    try {
      await api.delete(`/projects/${projectId}/tasks/${task.id}/comments/${commentId}`)
      loadComments()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete comment.')
    }
  }

  const handleUpload = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    setError('')
    try {
      const formData = new FormData()
      formData.append('file', file)
      await api.post(`/projects/${projectId}/tasks/${task.id}/attachments`, formData)
      loadAttachments()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload file.')
    } finally {
      setUploading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  const handleDownload = async (attachment) => {
    try {
      const res = await api.get(
        `/projects/${projectId}/tasks/${task.id}/attachments/${attachment.id}/download`,
        { responseType: 'blob' }
      )
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', attachment.filename)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      setError('Failed to download file.')
    }
  }

  const handleDeleteAttachment = async (attachmentId) => {
    try {
      await api.delete(`/projects/${projectId}/tasks/${task.id}/attachments/${attachmentId}`)
      loadAttachments()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete attachment.')
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal task-detail-modal" onClick={(e) => e.stopPropagation()}>
        <h3>{canEdit ? 'Edit Task' : task.title}</h3>
        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSave}>
          <label>Title</label>
          <input
            required
            disabled={!canEdit}
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />

          <label>Description</label>
          <textarea
            rows={3}
            disabled={!canEdit}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />

          <div className="form-row">
            <div>
              <label>Status</label>
              <select disabled={!canEdit} value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="DONE">Done</option>
              </select>
            </div>
            <div>
              <label>Priority</label>
              <select disabled={!canEdit} value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </div>
          </div>

          <label>Due Date</label>
          <input
            type="date"
            disabled={!canEdit}
            value={form.dueDate || ''}
            onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
          />

          <div className="modal-actions">
            {canEdit && <button type="button" className="btn btn-danger" onClick={handleDelete}>Delete</button>}
            <button type="button" className="btn btn-ghost" onClick={onClose}>Close</button>
            {canEdit && <button type="submit" className="btn btn-primary">Save</button>}
          </div>
        </form>

        <div className="task-detail-section">
          <h4>Attachments ({attachments.length})</h4>
          {attachments.map((a) => (
            <div className="attachment-item" key={a.id}>
              <span className="attachment-link" onClick={() => handleDownload(a)}>{a.filename}</span>
              <span className="attachment-size">
                {formatBytes(a.sizeBytes)}
                {canEdit && (
                  <button className="btn btn-ghost btn-sm" onClick={() => handleDeleteAttachment(a.id)}>Delete</button>
                )}
              </span>
            </div>
          ))}
          {canEdit && (
            <div className="upload-row">
              <input type="file" ref={fileInputRef} onChange={handleUpload} disabled={uploading} />
              {uploading && <span className="readonly-note">Uploading...</span>}
            </div>
          )}
        </div>

        <div className="task-detail-section">
          <h4>Comments ({comments.length})</h4>
          {comments.map((c) => (
            <div className="comment-item" key={c.id}>
              <div className="comment-meta">
                <span>{c.authorUsername} &middot; {new Date(c.createdAt).toLocaleString()}</span>
                {(c.authorId === user?.id || myRole === 'OWNER') && (
                  <button className="btn btn-ghost btn-sm" onClick={() => handleDeleteComment(c.id)}>Delete</button>
                )}
              </div>
              <div>{c.content}</div>
            </div>
          ))}
          {canEdit && (
            <form className="comment-form" onSubmit={handleAddComment}>
              <input
                placeholder="Write a comment..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
              />
              <button type="submit" className="btn btn-primary btn-sm">Post</button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
