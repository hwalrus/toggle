export type Toggle = { name: string; enabled: boolean }

export async function getAll(): Promise<Toggle[]> {
  const res = await fetch('/toggle')
  if (!res.ok) throw new Error(`Failed to fetch toggles: ${res.status}`)
  const data: Record<string, boolean> = await res.json()
  return Object.entries(data).map(([name, enabled]) => ({ name, enabled }))
}

export async function create(name: string, enabled: boolean): Promise<void> {
  const res = await fetch(`/toggle/${encodeURIComponent(name)}?enabled=${enabled}`, { method: 'POST' })
  if (!res.ok) throw new Error(`Failed to create toggle: ${res.status}`)
}

export async function enable(name: string): Promise<void> {
  const res = await fetch(`/toggle/${encodeURIComponent(name)}/enable`, { method: 'POST' })
  if (!res.ok) throw new Error(`Failed to enable toggle: ${res.status}`)
}

export async function disable(name: string): Promise<void> {
  const res = await fetch(`/toggle/${encodeURIComponent(name)}/disable`, { method: 'POST' })
  if (!res.ok) throw new Error(`Failed to disable toggle: ${res.status}`)
}

export async function remove(name: string): Promise<void> {
  const res = await fetch(`/toggle/${encodeURIComponent(name)}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(`Failed to delete toggle: ${res.status}`)
}
