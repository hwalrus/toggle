import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as api from '../api.ts'
import AddToggleForm from '../components/AddToggleForm.tsx'

vi.mock('../api.ts')

beforeEach(() => {
  vi.mocked(api.create).mockResolvedValue(undefined)
})

afterEach(() => {
  vi.clearAllMocks()
})

describe('AddToggleForm', () => {
  it('renders name input, enabled checkbox (checked by default), and a disabled Add button', () => {
    render(<AddToggleForm onCreated={vi.fn()} />)
    expect(screen.getByPlaceholderText('toggle-name')).toBeInTheDocument()
    expect(screen.getByRole('checkbox', { name: /enabled/i })).toBeChecked()
    expect(screen.getByRole('button', { name: /add/i })).toBeDisabled()
  })

  it('enables the Add button when name is non-empty with no spaces', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-feature')
    expect(screen.getByRole('button', { name: /add/i })).toBeEnabled()
  })

  it('keeps Add button disabled when name is only whitespace', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), '   ')
    expect(screen.getByRole('button', { name: /add/i })).toBeDisabled()
  })

  it('keeps Add button disabled when name contains a space', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my feature')
    expect(screen.getByRole('button', { name: /add/i })).toBeDisabled()
  })

  it('submitting calls create(name.trim(), true) and invokes onCreated', async () => {
    const user = userEvent.setup()
    const onCreated = vi.fn()
    render(<AddToggleForm onCreated={onCreated} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(api.create).toHaveBeenCalledWith('my-flag', true)
    expect(onCreated).toHaveBeenCalledTimes(1)
  })

  it('clears the name input after successful submission', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm onCreated={vi.fn()} />)
    const input = screen.getByPlaceholderText('toggle-name')
    await user.type(input, 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(input).toHaveValue('')
  })

  it('submitting with checkbox unchecked calls create(name, false)', async () => {
    const user = userEvent.setup()
    render(<AddToggleForm onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'dark-mode')
    await user.click(screen.getByRole('checkbox', { name: /enabled/i }))
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(api.create).toHaveBeenCalledWith('dark-mode', false)
  })

  it('disables form controls while submitting', async () => {
    vi.mocked(api.create).mockReturnValue(new Promise(() => {}))
    const user = userEvent.setup()
    render(<AddToggleForm onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'my-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(screen.getByPlaceholderText('toggle-name')).toBeDisabled()
    expect(screen.getByRole('checkbox', { name: /enabled/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /add/i })).toBeDisabled()
  })

  it('shows field error and does not call onCreated when create rejects', async () => {
    vi.mocked(api.create).mockRejectedValue(new Error('Failed to create toggle: 409'))
    const user = userEvent.setup()
    const onCreated = vi.fn()
    render(<AddToggleForm onCreated={onCreated} />)
    await user.type(screen.getByPlaceholderText('toggle-name'), 'existing')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(screen.getByText(/409/)).toBeInTheDocument()
    expect(onCreated).not.toHaveBeenCalled()
  })

  it('clears the field error on a subsequent successful submission', async () => {
    vi.mocked(api.create)
      .mockRejectedValueOnce(new Error('Failed to create toggle: 409'))
      .mockResolvedValue(undefined)
    const user = userEvent.setup()
    render(<AddToggleForm onCreated={vi.fn()} />)
    const input = screen.getByPlaceholderText('toggle-name')
    await user.type(input, 'existing')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(screen.getByText(/409/)).toBeInTheDocument()
    await user.clear(input)
    await user.type(input, 'new-flag')
    await user.click(screen.getByRole('button', { name: /add/i }))
    expect(screen.queryByText(/409/)).not.toBeInTheDocument()
  })
})
