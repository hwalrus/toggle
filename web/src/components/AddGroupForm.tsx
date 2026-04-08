import { useState, FormEvent } from 'react'
import { createGroup, namePattern, ApiError } from '../api.ts'

type Props = { onCreated: () => void }

export default function AddGroupForm({ onCreated }: Props) {
  const [name, setName] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const valid = namePattern.test(name)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!valid) return
    setSubmitting(true)
    setError(null)
    try {
      await createGroup(name)
      setName('')
      onCreated()
    } catch (err) {
      setError(err instanceof ApiError && err.status === 409 ? 'A group with this name already exists' : 'Failed to create group')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="add-form" onSubmit={handleSubmit}>
      <h2 className="section-title">New group</h2>
      <div className="add-form-row">
        <label htmlFor="group-name-input" className="sr-only">Group name</label>
        <input
          id="group-name-input"
          className="text-input"
          type="text"
          placeholder="group-name"
          value={name}
          onChange={e => setName(e.target.value)}
          disabled={submitting}
          spellCheck={false}
        />
        <button className="btn btn-primary" type="submit" disabled={!valid || submitting}>
          Add group
        </button>
      </div>
      {error && <p className="field-error" role="alert">{error}</p>}
    </form>
  )
}
