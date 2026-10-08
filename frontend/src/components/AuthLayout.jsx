import { useState } from 'react'
import Logo from './Logo.jsx'

const board = [
  { name: 'To do', cards: [['Design landing page', 'high', 'A'], ['Write API docs', 'low', 'R']] },
  { name: 'In progress', cards: [['Fix login bug', 'medium', 'S']] },
  { name: 'Done', cards: [['Set up database', 'done', 'A'], ['Ship v1 build', 'done', 'M']] },
]

function Showcase() {
  return (
    <aside className="auth-showcase" aria-hidden="true">
      <div className="showcase-board">
        {board.map((col) => (
          <div className="sb-col" key={col.name}>
            <div className="sb-col-title">{col.name}<span>{col.cards.length}</span></div>
            {col.cards.map(([title, tone, who]) => (
              <div className={`sb-card sb-${tone}`} key={title}>
                <span className="sb-check">{tone === 'done' && (
                  <svg viewBox="0 0 16 16" width="12" height="12"><path d="M3 8.5l3.2 3.2L13 5" fill="none" stroke="#fff" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" /></svg>
                )}</span>
                <span className="sb-title">{title}</span>
                <span className="sb-who">{who}</span>
              </div>
            ))}
          </div>
        ))}
      </div>
      <h1 className="showcase-headline">Plan it. Track it.<br />Kaam done.</h1>
      <p className="showcase-copy">
        One shared board for projects, tasks, files and comments, so your team always knows what's next.
      </p>
    </aside>
  )
}

export function PasswordField({ value, onChange, minLength, label = 'Password', placeholder }) {
  const [show, setShow] = useState(false)
  return (
    <>
      <label>{label}</label>
      <div className="pw-wrap">
        <input type={show ? 'text' : 'password'} value={value} onChange={onChange} required minLength={minLength} placeholder={placeholder} />
        <button type="button" className="pw-toggle" onClick={() => setShow(!show)} aria-label={show ? 'Hide password' : 'Show password'}>
          {show ? 'Hide' : 'Show'}
        </button>
      </div>
    </>
  )
}

export default function AuthLayout({ title, subtitle, onSubmit, error, children, footer }) {
  return (
    <div className="auth-page">
      <div className="auth-form-panel">
        <div className="auth-logo"><Logo size={34} /><span>Kaam Done</span></div>
        <form className="auth-card" onSubmit={onSubmit}>
          <h2>{title}</h2>
          <p className="auth-subtitle">{subtitle}</p>
          {error && <div className="alert-error" role="alert">{error}</div>}
          {children}
          <p className="auth-switch">{footer}</p>
        </form>
      </div>
      <Showcase />
    </div>
  )
}
