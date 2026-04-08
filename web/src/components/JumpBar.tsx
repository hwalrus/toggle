type Props = { groups: string[] }

export default function JumpBar({ groups }: Props) {
  function getCSSPixels(variable: string): number {
    return parseFloat(getComputedStyle(document.documentElement).getPropertyValue(variable))
  }

  function scrollToGroup(group: string) {
    const el = document.getElementById(`group-${group}`)
    if (!el) return
    const stickyOffset = getCSSPixels('--header-h') + getCSSPixels('--jump-bar-h')
    const top = el.getBoundingClientRect().top + window.scrollY - stickyOffset
    window.scrollTo({ top, behavior: 'smooth' })
  }

  return (
    <nav className="jump-bar" aria-label="Jump to group">
      {groups.map(group => (
        <button key={group} className="jump-bar-link" onClick={() => scrollToGroup(group)}>
          {group}
        </button>
      ))}
    </nav>
  )
}
