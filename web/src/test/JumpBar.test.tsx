import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import JumpBar from '../components/JumpBar.tsx'

describe('JumpBar', () => {
  it('renders a button for each group', () => {
    render(<JumpBar groups={['alpha', 'beta', 'gamma']} />)
    expect(screen.getByRole('button', { name: 'alpha' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'beta' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'gamma' })).toBeInTheDocument()
  })

  it('renders nothing when groups is empty', () => {
    const { container } = render(<JumpBar groups={[]} />)
    expect(container.querySelector('nav')!.childElementCount).toBe(0)
  })

  it('clicking a button does not throw when the target element is missing', async () => {
    const user = userEvent.setup()
    render(<JumpBar groups={['alpha']} />)
    await expect(user.click(screen.getByRole('button', { name: 'alpha' }))).resolves.not.toThrow()
  })

  it('clicking a button scrolls to the target element when it exists', async () => {
    const scrollTo = vi.fn()
    vi.stubGlobal('scrollTo', scrollTo)
    const el = document.createElement('div')
    el.id = 'group-alpha'
    el.getBoundingClientRect = () => ({ top: 200 } as DOMRect)
    document.body.appendChild(el)

    const user = userEvent.setup()
    render(<JumpBar groups={['alpha']} />)
    await user.click(screen.getByRole('button', { name: 'alpha' }))
    expect(scrollTo).toHaveBeenCalled()

    document.body.removeChild(el)
    vi.unstubAllGlobals()
  })
})
