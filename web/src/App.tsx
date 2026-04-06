import { useState, useEffect, useCallback } from 'react'
import { getGroups, getToggles, Toggle } from './api.ts'
import AddGroupForm from './components/AddGroupForm.tsx'
import GroupSection from './components/GroupSection.tsx'

export default function App() {
  const [groups, setGroups] = useState<string[]>([])
  const [togglesByGroup, setTogglesByGroup] = useState<Record<string, Toggle[]>>({})
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async () => {
    try {
      const fetchedGroups = await getGroups()
      const toggleArrays = await Promise.all(fetchedGroups.map(g => getToggles(g)))
      const byGroup: Record<string, Toggle[]> = {}
      fetchedGroups.forEach((g, i) => { byGroup[g] = toggleArrays[i] })
      setGroups(fetchedGroups)
      setTogglesByGroup(byGroup)
      setError(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { refresh() }, [refresh])

  return (
    <div className="page">
      <header className="header">
        <h1>Feature Toggles</h1>
      </header>
      <main className="main">
        <div className="card">
          <AddGroupForm onCreated={refresh} />
        </div>
        {error && <div className="error-banner" role="alert">{error}</div>}
        {!loading && groups.length === 0 && (
          <p className="empty-state">No groups yet. Add one above.</p>
        )}
        {loading && groups.length === 0 && (
          <p className="empty-state">Loading…</p>
        )}
        {groups.map(group => (
          <GroupSection
            key={group}
            group={group}
            toggles={togglesByGroup[group] ?? []}
            loading={loading}
            onGroupChanged={refresh}
            onToggleChanged={refresh}
          />
        ))}
      </main>
    </div>
  )
}
