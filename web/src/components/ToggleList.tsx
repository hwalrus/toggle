import { Toggle } from '../api.ts'
import ToggleRow from './ToggleRow.tsx'

type Props = { toggles: Toggle[]; onChanged: () => void }

export default function ToggleList({ toggles, onChanged }: Props) {
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
