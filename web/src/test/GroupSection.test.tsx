import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as api from '../api.ts'
import GroupSection from '../components/GroupSection.tsx'
import type { Toggle } from '../api.ts'

vi.mock('../api.ts')

const toggles: Toggle[] = [
  { group: 'payments', name: 'checkout', enabled: true },
]

beforeEach(() => {
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

describe('GroupSection', () => {
  it('renders the group name in the header', () => {
    render(<GroupSection group="payments" toggles={toggles} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    expect(screen.getByText('payments')).toBeInTheDocument()
  })

  it('renders toggle rows for the group', () => {
    render(<GroupSection group="payments" toggles={toggles} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    expect(screen.getByText('checkout')).toBeInTheDocument()
  })

  it('clicking Rename shows an inline input pre-filled with the group name', async () => {
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /rename/i }))
    const input = screen.getByLabelText('New group name')
    expect(input).toHaveValue('payments')
  })

  it('clicking Cancel hides the rename input', async () => {
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /rename/i }))
    await user.click(screen.getByRole('button', { name: /cancel/i }))
    expect(screen.queryByLabelText('New group name')).not.toBeInTheDocument()
    expect(screen.getByText('payments')).toBeInTheDocument()
  })

  it('submitting rename calls renameGroup and onGroupChanged', async () => {
    const user = userEvent.setup()
    const onGroupChanged = vi.fn()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={onGroupChanged} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /rename/i }))
    const input = screen.getByLabelText('New group name')
    await user.clear(input)
    await user.type(input, 'billing')
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(api.renameGroup).toHaveBeenCalledWith('payments', 'billing')
    expect(onGroupChanged).toHaveBeenCalledTimes(1)
  })

  it('clicking Delete shows a confirmation prompt', async () => {
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /^delete$/i }))
    expect(screen.getByText(/delete group/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /yes/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /no/i })).toBeInTheDocument()
  })

  it('clicking No cancels the delete', async () => {
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /^delete$/i }))
    await user.click(screen.getByRole('button', { name: /no/i }))
    expect(api.deleteGroup).not.toHaveBeenCalled()
    expect(screen.getByRole('button', { name: /^delete$/i })).toBeInTheDocument()
  })

  it('clicking Yes calls deleteGroup and onGroupChanged', async () => {
    const user = userEvent.setup()
    const onGroupChanged = vi.fn()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={onGroupChanged} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /^delete$/i }))
    await user.click(screen.getByRole('button', { name: /yes/i }))
    expect(api.deleteGroup).toHaveBeenCalledWith('payments')
    expect(onGroupChanged).toHaveBeenCalledTimes(1)
  })

  it('dismisses the confirm dialog after a successful delete', async () => {
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /^delete$/i }))
    await user.click(screen.getByRole('button', { name: /yes/i }))
    expect(screen.queryByRole('button', { name: /yes/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /no/i })).not.toBeInTheDocument()
  })

  it('shows an error when renameGroup rejects', async () => {
    vi.mocked(api.renameGroup).mockRejectedValue(new Error('Failed to rename group: 500'))
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /rename/i }))
    const input = screen.getByLabelText('New group name')
    await user.clear(input)
    await user.type(input, 'billing')
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(screen.getByRole('alert')).toBeInTheDocument()
  })

  it('shows an error when deleteGroup rejects', async () => {
    vi.mocked(api.deleteGroup).mockRejectedValue(new Error('Failed to delete group: 500'))
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /^delete$/i }))
    await user.click(screen.getByRole('button', { name: /yes/i }))
    expect(screen.getByRole('alert')).toBeInTheDocument()
  })

  it('disables action buttons while an operation is in flight', async () => {
    vi.mocked(api.deleteGroup).mockReturnValue(new Promise(() => {}))
    const user = userEvent.setup()
    render(<GroupSection group="payments" toggles={[]} loading={false} onGroupChanged={vi.fn()} onToggleChanged={vi.fn()} />)
    await user.click(screen.getByRole('button', { name: /^delete$/i }))
    await user.click(screen.getByRole('button', { name: /yes/i }))
    expect(screen.getByRole('button', { name: /yes/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /no/i })).toBeDisabled()
  })
})
