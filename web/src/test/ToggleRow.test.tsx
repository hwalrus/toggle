import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as api from '../api.ts'
import ToggleRow from '../components/ToggleRow.tsx'
import type { Toggle } from '../api.ts'

vi.mock('../api.ts')

const tog = (name: string, enabled: boolean): Toggle => ({ name, enabled })

beforeEach(() => {
  vi.mocked(api.enable).mockResolvedValue(undefined)
  vi.mocked(api.disable).mockResolvedValue(undefined)
  vi.mocked(api.remove).mockResolvedValue(undefined)
})

afterEach(() => {
  vi.clearAllMocks()
})

describe('ToggleRow — display', () => {
  it('renders the toggle name in a .toggle-name span', () => {
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    expect(screen.getByText('feature-x')).toHaveClass('toggle-name')
  })

  it('renders badge-enabled class for an enabled toggle', () => {
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    expect(screen.getByText('enabled')).toHaveClass('badge-enabled')
  })

  it('renders badge-disabled class for a disabled toggle', () => {
    render(<ToggleRow toggle={tog('feature-x', false)} onChanged={vi.fn()} />)
    expect(screen.getByText('disabled')).toHaveClass('badge-disabled')
  })

  it('switch checkbox is checked for an enabled toggle', () => {
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    // The switch input is unlabelled — find it by role within the switch label
    const checkboxes = screen.getAllByRole('checkbox')
    expect(checkboxes[0]).toBeChecked()
  })

  it('switch checkbox is unchecked for a disabled toggle', () => {
    render(<ToggleRow toggle={tog('feature-x', false)} onChanged={vi.fn()} />)
    const checkboxes = screen.getAllByRole('checkbox')
    expect(checkboxes[0]).not.toBeChecked()
  })

  it('shows the trash icon button initially and no confirm row', () => {
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    expect(screen.getByTitle('Delete')).toBeInTheDocument()
    expect(screen.queryByText('Delete?')).not.toBeInTheDocument()
  })
})

describe('ToggleRow — toggling enabled/disabled', () => {
  it('clicking switch on an enabled toggle calls disable(name) then onChanged', async () => {
    const user = userEvent.setup()
    const onChanged = vi.fn()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={onChanged} />)
    await user.click(screen.getAllByRole('checkbox')[0])
    expect(api.disable).toHaveBeenCalledWith('feature-x')
    expect(api.enable).not.toHaveBeenCalled()
    expect(onChanged).toHaveBeenCalledTimes(1)
  })

  it('clicking switch on a disabled toggle calls enable(name) then onChanged', async () => {
    const user = userEvent.setup()
    const onChanged = vi.fn()
    render(<ToggleRow toggle={tog('feature-x', false)} onChanged={onChanged} />)
    await user.click(screen.getAllByRole('checkbox')[0])
    expect(api.enable).toHaveBeenCalledWith('feature-x')
    expect(api.disable).not.toHaveBeenCalled()
    expect(onChanged).toHaveBeenCalledTimes(1)
  })

  it('disables the switch and delete button while the API call is in flight', async () => {
    vi.mocked(api.disable).mockReturnValue(new Promise(() => {}))
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getAllByRole('checkbox')[0])
    expect(screen.getAllByRole('checkbox')[0]).toBeDisabled()
    expect(screen.getByTitle('Delete')).toBeDisabled()
  })
})

describe('ToggleRow — delete confirmation flow', () => {
  it('clicking trash shows confirm row and hides trash button', async () => {
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getByTitle('Delete'))
    expect(screen.getByText('Delete?')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /yes/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /no/i })).toBeInTheDocument()
    expect(screen.queryByTitle('Delete')).not.toBeInTheDocument()
  })

  it('clicking No hides the confirm row and restores the trash button', async () => {
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getByTitle('Delete'))
    await user.click(screen.getByRole('button', { name: /no/i }))
    expect(screen.queryByText('Delete?')).not.toBeInTheDocument()
    expect(screen.getByTitle('Delete')).toBeInTheDocument()
  })

  it('clicking Yes calls remove(name) then onChanged', async () => {
    const user = userEvent.setup()
    const onChanged = vi.fn()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={onChanged} />)
    await user.click(screen.getByTitle('Delete'))
    await user.click(screen.getByRole('button', { name: /yes/i }))
    expect(api.remove).toHaveBeenCalledWith('feature-x')
    expect(onChanged).toHaveBeenCalledTimes(1)
  })

  it('does not call remove on the first trash click', async () => {
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getByTitle('Delete'))
    expect(api.remove).not.toHaveBeenCalled()
  })

  it('disables the Yes button while delete is in flight', async () => {
    vi.mocked(api.remove).mockReturnValue(new Promise(() => {}))
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getByTitle('Delete'))
    await user.click(screen.getByRole('button', { name: /yes/i }))
    expect(screen.getByRole('button', { name: /yes/i })).toBeDisabled()
  })
})

describe('ToggleRow — error handling', () => {
  it('shows an error when disable rejects', async () => {
    vi.mocked(api.disable).mockRejectedValue(new Error('Failed to disable toggle: 500'))
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getAllByRole('checkbox')[0])
    expect(screen.getByRole('alert')).toHaveTextContent('500')
  })

  it('shows an error when remove rejects', async () => {
    vi.mocked(api.remove).mockRejectedValue(new Error('Failed to delete toggle: 404'))
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getByTitle('Delete'))
    await user.click(screen.getByRole('button', { name: /yes/i }))
    expect(screen.getByRole('alert')).toHaveTextContent('404')
  })

  it('clears the error when the switch is clicked again', async () => {
    vi.mocked(api.disable)
      .mockRejectedValueOnce(new Error('Request failed'))
      .mockResolvedValue(undefined)
    const user = userEvent.setup()
    render(<ToggleRow toggle={tog('feature-x', true)} onChanged={vi.fn()} />)
    await user.click(screen.getAllByRole('checkbox')[0])
    expect(screen.getByRole('alert')).toBeInTheDocument()
    await user.click(screen.getAllByRole('checkbox')[0])
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })
})
