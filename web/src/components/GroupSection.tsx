import { useState } from 'react'
import { Toggle, renameGroup, deleteGroup, namePattern } from '../api.ts'
import AddToggleForm from './AddToggleForm.tsx'
import ToggleList from './ToggleList.tsx'
import { useAsyncAction } from '../hooks/useAsyncAction.ts'

type Props = {
  group: string
  toggles: Toggle[]
  loading: boolean
  onGroupChanged: () => void
  onToggleChanged: () => void
}

export default function GroupSection({ group, toggles, loading, onGroupChanged, onToggleChanged }: Props) {
  const [renaming, setRenaming] = useState(false)
  const [newName, setNewName] = useState('')
  const [confirmDelete, setConfirmDelete] = useState(false)
  const { busy, error, run } = useAsyncAction()

  const renameValid = namePattern.test(newName)

  async function handleRename() {
    if (!renameValid) return
    const ok = await run(() => renameGroup(group, newName), 'Failed to rename group')
    if (ok) {
      setRenaming(false)
      setNewName('')
      onGroupChanged()
    }
  }

  async function handleDelete() {
    const ok = await run(() => deleteGroup(group), 'Failed to delete group')
    if (ok) {
      onGroupChanged()
    } else {
      setConfirmDelete(false)
    }
  }

  return (
    <div className="card group-section">
      <div className="group-header">
        {renaming ? (
          <div className="group-rename-row">
            <label htmlFor={`rename-${group}`} className="sr-only">New group name</label>
            <input
              id={`rename-${group}`}
              className="text-input"
              type="text"
              value={newName}
              onChange={e => setNewName(e.target.value)}
              disabled={busy}
              spellCheck={false}
              autoFocus
            />
            <button className="btn btn-primary btn-sm" onClick={handleRename} disabled={!renameValid || busy}>
              Save
            </button>
            <button className="btn btn-ghost btn-sm" onClick={() => { setRenaming(false); setNewName('') }} disabled={busy}>
              Cancel
            </button>
          </div>
        ) : (
          <>
            <span className="group-title">{group}</span>
            <div className="group-actions">
              {confirmDelete ? (
                <>
                  <span className="confirm-label">Delete group and all its toggles?</span>
                  <button className="btn btn-danger btn-sm" onClick={handleDelete} disabled={busy}>Yes</button>
                  <button className="btn btn-ghost btn-sm" onClick={() => setConfirmDelete(false)} disabled={busy}>No</button>
                </>
              ) : (
                <>
                  <button className="btn btn-ghost btn-sm" onClick={() => { setRenaming(true); setNewName(group) }} disabled={busy}>
                    Rename
                  </button>
                  <button className="btn btn-ghost btn-sm" onClick={() => setConfirmDelete(true)} disabled={busy} title="Delete group">
                    Delete
                  </button>
                </>
              )}
            </div>
          </>
        )}
      </div>
      {error && <span className="row-error" role="alert">{error}</span>}
      <AddToggleForm group={group} onCreated={onToggleChanged} />
      <ToggleList toggles={toggles} loading={loading} onChanged={onToggleChanged} />
    </div>
  )
}
