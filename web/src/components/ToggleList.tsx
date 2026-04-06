import { Toggle } from '../api.ts'
import ToggleRow from './ToggleRow.tsx'

type Props = { toggles: Toggle[]; loading: boolean; onChanged: () => void }

export default function ToggleList({ toggles, loading, onChanged }: Props) {
  if (loading) {
    return <p className="empty-state">Loading…</p>
  }
  if (toggles.length === 0) {
    return <p className="empty-state">No toggles yet. Add one above.</p>
  }

  return (
    <ul className="toggle-list">
      {toggles.map(toggle => (
        <ToggleRow key={toggle.name} toggle={toggle} onChanged={onChanged} />
      ))}
    </ul>
  )
}
