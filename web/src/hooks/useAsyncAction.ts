import { useState } from 'react'

export function useAsyncAction() {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function run(action: () => Promise<void>, fallback = 'Request failed'): Promise<boolean> {
    setBusy(true)
    setError(null)
    try {
      await action()
      return true
    } catch (err) {
      setError(err instanceof Error ? err.message : fallback)
      return false
    } finally {
      setBusy(false)
    }
  }

  return { busy, error, run }
}
