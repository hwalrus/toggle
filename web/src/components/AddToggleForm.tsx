import { useState, FormEvent } from 'react'
import { create, namePattern, ApiError } from '../api.ts'

type Props = { group: string; onCreated: () => void }

export default function AddToggleForm({ group, onCreated }: Props) {
  const [name, setName] = useState('')
  const [enabled, setEnabled] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const valid = namePattern.test(name)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!valid) return
    setSubmitting(true)
    setError(null)
    try {
      await create(group, name, enabled)
      setName('')
      setEnabled(true)
      onCreated()
    } catch (err) {
      setError(err instanceof ApiError && err.status === 409 ? 'A toggle with this name already exists in this group' : 'Failed to create toggle')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="add-form" onSubmit={handleSubmit}>
      <h2 className="section-title">New toggle</h2>
      <div className="add-form-row">
        <label htmlFor={`toggle-name-input-${group}`} className="sr-only">Toggle name</label>
        <input
          id={`toggle-name-input-${group}`}
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
      {error && <p className="field-error" role="alert">{error}</p>}
    </form>
  )
}
