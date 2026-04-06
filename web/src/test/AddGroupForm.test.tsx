import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as api from '../api.ts'
import AddGroupForm from '../components/AddGroupForm.tsx'

vi.mock('../api.ts')

beforeEach(() => {
  vi.mocked(api.createGroup).mockResolvedValue(undefined)
})

afterEach(() => {
  vi.clearAllMocks()
})

describe('AddGroupForm', () => {
  it('renders name input and a disabled Add group button', () => {
    render(<AddGroupForm onCreated={vi.fn()} />)
    expect(screen.getByPlaceholderText('group-name')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /add group/i })).toBeDisabled()
  })

  it('enables the button when a valid name is entered', async () => {
    const user = userEvent.setup()
    render(<AddGroupForm onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('group-name'), 'payments')
    expect(screen.getByRole('button', { name: /add group/i })).toBeEnabled()
  })

  it('keeps button disabled when name contains a space', async () => {
    const user = userEvent.setup()
    render(<AddGroupForm onCreated={vi.fn()} />)
    await user.type(screen.getByPlaceholderText('group-name'), 'my group')
    expect(screen.getByRole('button', { name: /add group/i })).toBeDisabled()
  })

  it('calls createGroup and invokes onCreated on submit', async () => {
    const user = userEvent.setup()
    const onCreated = vi.fn()
    render(<AddGroupForm onCreated={onCreated} />)
    await user.type(screen.getByPlaceholderText('group-name'), 'payments')
    await user.click(screen.getByRole('button', { name: /add group/i }))
    expect(api.createGroup).toHaveBeenCalledWith('payments')
    expect(onCreated).toHaveBeenCalledTimes(1)
  })

  it('clears the input after successful submission', async () => {
    const user = userEvent.setup()
    render(<AddGroupForm onCreated={vi.fn()} />)
    const input = screen.getByPlaceholderText('group-name')
    await user.type(input, 'payments')
    await user.click(screen.getByRole('button', { name: /add group/i }))
    expect(input).toHaveValue('')
  })

  it('shows error and does not call onCreated when createGroup rejects', async () => {
    vi.mocked(api.createGroup).mockRejectedValue(new Error('Failed to create group: 409'))
    const user = userEvent.setup()
    const onCreated = vi.fn()
    render(<AddGroupForm onCreated={onCreated} />)
    await user.type(screen.getByPlaceholderText('group-name'), 'existing')
    await user.click(screen.getByRole('button', { name: /add group/i }))
    expect(screen.getByText(/409/)).toBeInTheDocument()
    expect(onCreated).not.toHaveBeenCalled()
  })
})
