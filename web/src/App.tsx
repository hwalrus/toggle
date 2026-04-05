import { useState, useEffect, useCallback } from 'react'
import { getAll, Toggle } from './api.ts'
import AddToggleForm from './components/AddToggleForm.tsx'
import ToggleList from './components/ToggleList.tsx'

export default function App() {
  const [toggles, setToggles] = useState<Toggle[]>([])
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(async () => {
    try {
      setToggles(await getAll())
      setError(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error')
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
          <AddToggleForm onCreated={refresh} />
        </div>
        {error && <div className="error-banner" role="alert">{error}</div>}
        <div className="card">
          <ToggleList toggles={toggles} onChanged={refresh} />
        </div>
      </main>
    </div>
  )
}
