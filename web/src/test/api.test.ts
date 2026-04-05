import { getAll, create, enable, disable, remove } from '../api.ts'

function mockFetch(ok: boolean, body?: unknown, status = 200) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok,
    status,
    json: () => Promise.resolve(body),
  }))
}

afterEach(() => {
  vi.restoreAllMocks()
})

describe('getAll', () => {
  it('calls GET /toggle and maps Record<string,boolean> to Toggle[]', async () => {
    mockFetch(true, { featureA: true, featureB: false })
    const result = await getAll()
    expect(result).toEqual([
      { name: 'featureA', enabled: true },
      { name: 'featureB', enabled: false },
    ])
    expect(fetch).toHaveBeenCalledWith('/toggle')
  })

  it('returns [] when backend returns an empty object', async () => {
    mockFetch(true, {})
    expect(await getAll()).toEqual([])
  })

  it('throws with status code when response is not ok', async () => {
    mockFetch(false, undefined, 500)
    await expect(getAll()).rejects.toThrow('500')
  })
})

describe('create', () => {
  it('calls POST /toggle/{name}?enabled=true', async () => {
    mockFetch(true)
    await create('my-flag', true)
    expect(fetch).toHaveBeenCalledWith('/toggle/my-flag?enabled=true', { method: 'POST' })
  })

  it('calls POST /toggle/{name}?enabled=false when disabled', async () => {
    mockFetch(true)
    await create('dark-mode', false)
    expect(fetch).toHaveBeenCalledWith('/toggle/dark-mode?enabled=false', { method: 'POST' })
  })

  it('URL-encodes the toggle name', async () => {
    mockFetch(true)
    await create('my flag', true)
    const url = (fetch as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('my%20flag')
  })

  it('throws when response is not ok', async () => {
    mockFetch(false, undefined, 409)
    await expect(create('x', true)).rejects.toThrow('409')
  })
})

describe('enable', () => {
  it('calls POST /toggle/{name}/enable', async () => {
    mockFetch(true)
    await enable('my-flag')
    expect(fetch).toHaveBeenCalledWith('/toggle/my-flag/enable', { method: 'POST' })
  })

  it('URL-encodes the name', async () => {
    mockFetch(true)
    await enable('my flag')
    const url = (fetch as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('my%20flag')
  })

  it('throws when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    await expect(enable('x')).rejects.toThrow('404')
  })
})

describe('disable', () => {
  it('calls POST /toggle/{name}/disable', async () => {
    mockFetch(true)
    await disable('my-flag')
    expect(fetch).toHaveBeenCalledWith('/toggle/my-flag/disable', { method: 'POST' })
  })

  it('URL-encodes the name', async () => {
    mockFetch(true)
    await disable('my flag')
    const url = (fetch as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('my%20flag')
  })

  it('throws when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    await expect(disable('x')).rejects.toThrow('404')
  })
})

describe('remove', () => {
  it('calls DELETE /toggle/{name}', async () => {
    mockFetch(true)
    await remove('my-flag')
    expect(fetch).toHaveBeenCalledWith('/toggle/my-flag', { method: 'DELETE' })
  })

  it('URL-encodes the name', async () => {
    mockFetch(true)
    await remove('my flag')
    const url = (fetch as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('my%20flag')
  })

  it('throws when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    await expect(remove('x')).rejects.toThrow('404')
  })
})
