import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as api from '../api.ts'
import { ApiError } from '../api.ts'
import AddToggleForm from '../components/AddToggleForm.tsx'

vi.mock('../api.ts', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../api.ts')>()
  return { ...actual, create: vi.fn() }
})

beforeEach(() => {
  vi.mocked(api.create).mockResolvedValue(undefined)
})

afterEach(() => {
  vi.clearAllMocks()
})

describe('AddToggleForm', () => {
  it('renders name input, enabled checkbox (checked by default), and a disabled Add button', () => {
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    expect(screen.getByPlaceholderText('toggle-name')).toBeInTheDocument()
    expect(screen.getByRole('checkbox', { name: /enabled/i })).toBeChecked()
    expect(screen.getByRole('button', { name: /add/i })).toBeDisabled()
  })

  it('enables the Add button when name is valid', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-feature')
    expect(screen.getByRole('button', { name: /add/i })).toBeEnabled()
  })

  it('keeps Add button disabled when name contains a space', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my feature')
    expect(screen.getByRole('button', { name: /add/i })).toBeDisabled()
  })

  it('submitting calls create(group, name, true) and invokes onCreated', async () => {
    const user = userEvent.setup()
    const onCreated = vi.fn()
    render(<AddToggleForm group="payments" onCreated={onCreated} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(api.create).toHaveBeenCalledWith('payments', 'my-flag', true)
    expect(onCreated).toHaveBeenCalledTimes(1)
  })

  it('clears the name input after successful submission', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    const input = screen.getByPlaceholderText('toggle-name')
    await user.type(input, 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(input).toHaveValue('')
  })

  it('submitting with checkbox unchecked calls create(group, name, false)', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'dark-mode')
    await user.click(screen.getByRole('checkbox', { name: /enabled/i }))
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(api.create).toHaveBeenCalledWith('g', 'dark-mode', false)
  })

  it('disables form controls while submitting', async () => {
    vi.mocked(api.create).mockReturnValue(new Promise(() => {}))
    const user = userEvent.setup()
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(screen.getByPlaceholderText('toggle-name')).toBeDisabled()
    expect(screen.getByRole('checkbox', { name: /enabled/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /add/i })).toBeDisabled()
  })

  it('shows a conflict message when the toggle name is already taken', async () => {
    vi.mocked(api.create).mockRejectedValue(new ApiError(409, 'Failed to create toggle: 409'))
    const user = userEvent.setup()
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(screen.getByText(/already exists/i)).toBeInTheDocument()
  })

  it('shows a generic error for non-conflict failures', async () => {
    vi.mocked(api.create).mockRejectedValue(new ApiError(500, 'Failed to create toggle: 500'))
    const user = userEvent.setup()
    render(<AddToggleForm group="g" onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(screen.getByText(/failed to create toggle/i)).toBeInTheDocument()
  })
})
