export type Toggle = { group: string; name: string; enabled: boolean }

// ── Groups ────────────────────────────────────────────────────────────────────

export async function getGroups(): Promise<string[]> {
  const res = await fetch('/group')
  if (!res.ok) throw new Error(`Failed to fetch groups: ${res.status}`)
  return res.json()
}

export async function createGroup(name: string): Promise<void> {
  const res = await fetch(`/group/${encodeURIComponent(name)}`, { method: 'POST' })
  if (!res.ok) throw new Error(`Failed to create group: ${res.status}`)
}

export async function renameGroup(name: string, newName: string): Promise<void> {
  const res = await fetch(
    `/group/${encodeURIComponent(name)}/rename?name=${encodeURIComponent(newName)}`,
    { method: 'POST' }
  )
  if (!res.ok) throw new Error(`Failed to rename group: ${res.status}`)
}

export async function deleteGroup(name: string): Promise<void> {
  const res = await fetch(`/group/${encodeURIComponent(name)}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(`Failed to delete group: ${res.status}`)
}

// ── Toggles ───────────────────────────────────────────────────────────────────

export async function getToggles(group: string): Promise<Toggle[]> {
  const res = await fetch(`/group/${encodeURIComponent(group)}/toggle`)
  if (!res.ok) throw new Error(`Failed to fetch toggles: ${res.status}`)
  const data: Record<string, boolean> = await res.json()
  return Object.entries(data).map(([name, enabled]) => ({ group, name, enabled }))
}

export async function create(group: string, name: string, enabled: boolean): Promise<void> {
  const res = await fetch(
    `/group/${encodeURIComponent(group)}/toggle/${encodeURIComponent(name)}?enabled=${enabled}`,
    { method: 'POST' }
  )
  if (!res.ok) throw new Error(`Failed to create toggle: ${res.status}`)
}

export async function enable(group: string, name: string): Promise<void> {
  const res = await fetch(
    `/group/${encodeURIComponent(group)}/toggle/${encodeURIComponent(name)}/enable`,
    { method: 'POST' }
  )
  if (!res.ok) throw new Error(`Failed to enable toggle: ${res.status}`)
}

export async function disable(group: string, name: string): Promise<void> {
  const res = await fetch(
    `/group/${encodeURIComponent(group)}/toggle/${encodeURIComponent(name)}/disable`,
    { method: 'POST' }
  )
  if (!res.ok) throw new Error(`Failed to disable toggle: ${res.status}`)
}

export async function remove(group: string, name: string): Promise<void> {
  const res = await fetch(
    `/group/${encodeURIComponent(group)}/toggle/${encodeURIComponent(name)}`,
    { method: 'DELETE' }
  )
  if (!res.ok) throw new Error(`Failed to delete toggle: ${res.status}`)
}
