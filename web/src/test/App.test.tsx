import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as api from '../api.ts'
import App from '../App.tsx'

vi.mock('../api.ts')

const defaultToggles = [
  { name: 'alpha', enabled: true },
  { name: 'beta', enabled: false },
]

beforeEach(() => {
  vi.mocked(api.getAll).mockResolvedValue(defaultToggles)
  vi.mocked(api.create).mockResolvedValue(undefined)
  vi.mocked(api.enable).mockResolvedValue(undefined)
  vi.mocked(api.disable).mockResolvedValue(undefined)
  vi.mocked(api.remove).mockResolvedValue(undefined)
})

afterEach(() => {
  vi.clearAllMocks()
})

describe('App', () => {
  it('fetches toggles on mount and renders them', async () => {
    render(<App />)
    expect(await screen.findByText('alpha')).toBeInTheDocument()
    expect(screen.getByText('beta')).toBeInTheDocument()
    expect(api.getAll).toHaveBeenCalledTimes(1)
  })

  it('shows error banner when getAll fails', async () => {
    vi.mocked(api.getAll).mockRejectedValue(new Error('Failed to fetch toggles: 503'))
    render(<App />)
    expect(await screen.findByText(/503/)).toBeInTheDocument()
    expect(screen.queryByText('alpha')).not.toBeInTheDocument()
  })

  it('renders the "New toggle" form heading', async () => {
    render(<App />)
    await screen.findByText('alpha')
    expect(screen.getByRole('heading', { name: /new toggle/i })).toBeInTheDocument()
  })

  it('shows empty-state message when getAll returns []', async () => {
    vi.mocked(api.getAll).mockResolvedValue([])
    render(<App />)
    expect(await screen.findByText('No toggles yet. Add one above.')).toBeInTheDocument()
  })

  it('adding a toggle calls create, refreshes the list, and shows the new toggle', async () => {
    vi.mocked(api.getAll)
      .mockResolvedValueOnce(defaultToggles)
      .mockResolvedValueOnce([...defaultToggles, { name: 'gamma', enabled: true }])
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText('alpha')
    await user.type(screen.getByPlaceholderText('toggle-name'), 'gamma')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(api.create).toHaveBeenCalledWith('gamma', true)
    expect(api.getAll).toHaveBeenCalledTimes(2)
    expect(await screen.findByText('gamma')).toBeInTheDocument()
  })
})
