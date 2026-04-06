import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as api from '../api.ts'
import App from '../App.tsx'

vi.mock('../api.ts')

const defaultGroups = ['alpha', 'beta']
const defaultToggles: Record<string, api.Toggle[]> = {
  alpha: [{ group: 'alpha', name: 'feat1', enabled: true }],
  beta:  [{ group: 'beta',  name: 'feat2', enabled: false }],
}

beforeEach(() => {
  vi.mocked(api.getGroups).mockResolvedValue(defaultGroups)
  vi.mocked(api.getToggles).mockImplementation(g => Promise.resolve(defaultToggles[g] ?? []))
  vi.mocked(api.createGroup).mockResolvedValue(undefined)
  vi.mocked(api.renameGroup).mockResolvedValue(undefined)
  vi.mocked(api.deleteGroup).mockResolvedValue(undefined)
  vi.mocked(api.create).mockResolvedValue(undefined)
  vi.mocked(api.enable).mockResolvedValue(undefined)
  vi.mocked(api.disable).mockResolvedValue(undefined)
  vi.mocked(api.remove).mockResolvedValue(undefined)
})

afterEach(() => {
  vi.clearAllMocks()
})

describe('App', () => {
  it('fetches groups and toggles on mount and renders them', async () => {
    render(<App />)
    expect(await screen.findByText('feat1')).toBeInTheDocument()
    expect(screen.getByText('feat2')).toBeInTheDocument()
    expect(api.getGroups).toHaveBeenCalledTimes(1)
    expect(api.getToggles).toHaveBeenCalledWith('alpha')
    expect(api.getToggles).toHaveBeenCalledWith('beta')
  })

  it('shows group names as section headers', async () => {
    render(<App />)
    expect(await screen.findByText('alpha')).toBeInTheDocument()
    expect(screen.getByText('beta')).toBeInTheDocument()
  })

  it('shows error banner when getGroups fails', async () => {
    vi.mocked(api.getGroups).mockRejectedValue(new Error('Failed to fetch groups: 503'))
    render(<App />)
    expect(await screen.findByText(/503/)).toBeInTheDocument()
  })

  it('shows loading indicator before the initial fetch resolves', async () => {
    let resolve!: (v: string[]) => void
    vi.mocked(api.getGroups).mockReturnValueOnce(new Promise(r => { resolve = r }))
    render(<App />)
    expect(screen.getByText('Loading…')).toBeInTheDocument()
    resolve(defaultGroups)
    expect(await screen.findByText('feat1')).toBeInTheDocument()
  })

  it('shows empty-state message when there are no groups', async () => {
    vi.mocked(api.getGroups).mockResolvedValue([])
    render(<App />)
    expect(await screen.findByText('No groups yet. Add one above.')).toBeInTheDocument()
  })

  it('renders the "New group" form heading', async () => {
    render(<App />)
    await screen.findByText('feat1')
    expect(screen.getByRole('heading', { name: /new group/i })).toBeInTheDocument()
  })

  it('adding a group calls createGroup and refreshes', async () => {
    vi.mocked(api.getGroups)
      .mockResolvedValueOnce(defaultGroups)
      .mockResolvedValueOnce([...defaultGroups, 'gamma'])
    vi.mocked(api.getToggles).mockResolvedValue([])
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText('alpha')
    await user.type(screen.getByPlaceholderText('group-name'), 'gamma')
    await user.click(screen.getByRole('button', { name: /add group/i }))
    expect(api.createGroup).toHaveBeenCalledWith('gamma')
    expect(api.getGroups).toHaveBeenCalledTimes(2)
    expect(await screen.findByText('gamma')).toBeInTheDocument()
  })
})
