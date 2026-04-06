import { useState } from 'react'
import { Toggle, enable, disable, remove } from '../api.ts'

type Props = { toggle: Toggle; onChanged: () => void }

export default function ToggleRow({ toggle, onChanged }: Props) {
  const [busy, setBusy] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleToggle() {
    setBusy(true)
    setError(null)
    try {
      if (toggle.enabled) await disable(toggle.group, toggle.name)
      else await enable(toggle.group, toggle.name)
      onChanged()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed')
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete() {
    if (!confirmDelete) {
      setConfirmDelete(true)
      return
    }
    setBusy(true)
    setError(null)
    try {
      await remove(toggle.group, toggle.name)
      onChanged()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed')
    } finally {
      setBusy(false)
      setConfirmDelete(false)
    }
  }

  return (
    <li className="toggle-row">
      <span className="toggle-name">{toggle.name}</span>
      <span className={`badge ${toggle.enabled ? 'badge-enabled' : 'badge-disabled'}`}>
        {toggle.enabled ? 'enabled' : 'disabled'}
      </span>
      <label className="switch" title={toggle.enabled ? 'Disable' : 'Enable'}>
        <input
          type="checkbox"
          checked={toggle.enabled}
          onChange={handleToggle}
          disabled={busy}
        />
        <span className="switch-track" />
      </label>
      {confirmDelete ? (
        <span className="confirm-row">
          <span className="confirm-label">Delete?</span>
          <button className="btn btn-danger btn-sm" onClick={handleDelete} disabled={busy}>Yes</button>
          <button className="btn btn-ghost btn-sm" onClick={() => setConfirmDelete(false)}>No</button>
        </span>
      ) : (
        <button className="btn btn-ghost btn-sm icon-btn" onClick={() => setConfirmDelete(true)} disabled={busy} title="Delete">
          <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
            <path d="M5.5 5.5a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5.5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"/>
            <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3a1 1 0 0 1 1 1M4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"/>
          </svg>
        </button>
      )}
      {error && <span className="row-error" role="alert">{error}</span>}
    </li>
  )
}
