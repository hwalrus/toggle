import { useState, FormEvent } from 'react'
import { create } from '../api.ts'

type Props = { onCreated: () => void }

export default function AddToggleForm({ onCreated }: Props) {
  const [name, setName] = useState('')
  const [enabled, setEnabled] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const valid = name.trim().length > 0 && !/\s/.test(name)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!valid) return
    setSubmitting(true)
    setError(null)
    try {
      await create(name.trim(), enabled)
      setName('')
      setEnabled(true)
      onCreated()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create toggle')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="add-form" onSubmit={handleSubmit}>
      <h2 className="section-title">New toggle</h2>
      <div className="add-form-row">
        <input
          className="text-input"
          type="text"
          placeholder="toggle-name"
          value={name}
          onChange={e => setName(e.target.value)}
          disabled={submitting}
          spellCheck={false}
        />
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={enabled}
            onChange={e => setEnabled(e.target.checked)}
            disabled={submitting}
          />
          Enabled
        </label>
        <button className="btn btn-primary" type="submit" disabled={!valid || submitting}>
          Add
        </button>
      </div>
      {error && <p className="field-error">{error}</p>}
    </form>
  )
}
