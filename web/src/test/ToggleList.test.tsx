import { render, screen } from '@testing-library/react'
import * as api from '../api.ts'
import ToggleList from '../components/ToggleList.tsx'
import type { Toggle } from '../api.ts'

vi.mock('../api.ts')

beforeEach(() => {
  vi.mocked(api.enable).mockResolvedValue(undefined)
  vi.mocked(api.disable).mockResolvedValue(undefined)
  vi.mocked(api.remove).mockResolvedValue(undefined)
})

afterEach(() => {
  vi.clearAllMocks()
})

const toggles: Toggle[] = [
  { name: 'alpha', enabled: true },
  { name: 'beta', enabled: false },
]

describe('ToggleList', () => {
  it('renders loading indicator when loading is true', () => {
    render(<ToggleList toggles={[]} loading={true} onChanged={vi.fn()} />)
    expect(screen.getByText('Loading…')).toBeInTheDocument()
    expect(screen.queryByRole('list')).not.toBeInTheDocument()
    expect(screen.queryByText(/No toggles yet/)).not.toBeInTheDocument()
  })

  it('renders empty-state message and no list when toggles is empty', () => {
    render(<ToggleList toggles={[]} loading={false} onChanged={vi.fn()} />)
    expect(screen.getByText('No toggles yet. Add one above.')).toBeInTheDocument()
    expect(screen.queryByRole('list')).not.toBeInTheDocument()
  })

  it('renders one list item per toggle', () => {
    render(<ToggleList toggles={toggles} loading={false} onChanged={vi.fn()} />)
    expect(screen.getAllByRole('listitem')).toHaveLength(2)
  })

  it('renders each toggle name', () => {
    render(<ToggleList toggles={toggles} loading={false} onChanged={vi.fn()} />)
    expect(screen.getByText('alpha')).toBeInTheDocument()
    expect(screen.getByText('beta')).toBeInTheDocument()
  })

  it('renders badge-enabled class for enabled toggle and badge-disabled for disabled', () => {
    render(<ToggleList toggles={toggles} loading={false} onChanged={vi.fn()} />)
    expect(screen.getByText('enabled')).toHaveClass('badge-enabled')
    expect(screen.getByText('disabled')).toHaveClass('badge-disabled')
  })

  it('does not show empty-state when toggles are present', () => {
    render(<ToggleList toggles={toggles} loading={false} onChanged={vi.fn()} />)
    expect(screen.queryByText(/No toggles yet/)).not.toBeInTheDocument()
  })
})
