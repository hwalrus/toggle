type Props = { groups: string[] }

export default function JumpBar({ groups }: Props) {
  function scrollToGroup(group: string) {
    const el = document.getElementById(`group-${group}`)
    if (!el) return
    const stickyOffset = parseInt(getComputedStyle(document.documentElement).getPropertyValue('--header-h')) +
                         parseInt(getComputedStyle(document.documentElement).getPropertyValue('--jump-bar-h'))
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
