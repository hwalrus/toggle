import { getGroups, createGroup, renameGroup, deleteGroup, getToggles, create, enable, disable, remove, ApiError } from '../api.ts'

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

describe('getGroups', () => {
  it('calls GET /group and returns the list', async () => {
    mockFetch(true, ['payments', 'ui'])
    const result = await getGroups()
    expect(result).toEqual(['payments', 'ui'])
    expect(fetch).toHaveBeenCalledWith('/group')
  })

  it('returns [] when backend returns empty list', async () => {
    mockFetch(true, [])
    expect(await getGroups()).toEqual([])
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 500)
    const err = await getGroups().catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(500)
  })
})

describe('createGroup', () => {
  it('calls POST /group/{name}', async () => {
    mockFetch(true)
    await createGroup('payments')
    expect(fetch).toHaveBeenCalledWith('/group/payments', { method: 'POST' })
  })

  it('URL-encodes the group name', async () => {
    mockFetch(true)
    await createGroup('my group')
    const url = (fetch as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('my%20group')
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 409)
    const err = await createGroup('x').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(409)
  })
})

describe('renameGroup', () => {
  it('calls POST /group/{name}/rename?name={newName}', async () => {
    mockFetch(true)
    await renameGroup('old', 'new')
    expect(fetch).toHaveBeenCalledWith('/group/old/rename?name=new', { method: 'POST' })
  })

  it('URL-encodes both names', async () => {
    mockFetch(true)
    await renameGroup('my group', 'new name')
    const url = (fetch as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('my%20group')
    expect(url).toContain('new%20name')
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    const err = await renameGroup('x', 'y').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(404)
  })
})

describe('deleteGroup', () => {
  it('calls DELETE /group/{name}', async () => {
    mockFetch(true)
    await deleteGroup('payments')
    expect(fetch).toHaveBeenCalledWith('/group/payments', { method: 'DELETE' })
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    const err = await deleteGroup('x').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(404)
  })
})

describe('getToggles', () => {
  it('calls GET /group/{group}/toggle and maps to Toggle[]', async () => {
    mockFetch(true, { featureA: true, featureB: false })
    const result = await getToggles('payments')
    expect(result).toEqual([
      { group: 'payments', name: 'featureA', enabled: true },
      { group: 'payments', name: 'featureB', enabled: false },
    ])
    expect(fetch).toHaveBeenCalledWith('/group/payments/toggle')
  })

  it('returns [] when group has no toggles', async () => {
    mockFetch(true, {})
    expect(await getToggles('g')).toEqual([])
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    const err = await getToggles('x').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(404)
  })
})

describe('create', () => {
  it('calls POST /group/{group}/toggle/{name}?enabled=true', async () => {
    mockFetch(true)
    await create('payments', 'checkout', true)
    expect(fetch).toHaveBeenCalledWith(
      '/group/payments/toggle/checkout?enabled=true',
      { method: 'POST' }
    )
  })

  it('URL-encodes group and toggle name', async () => {
    mockFetch(true)
    await create('my group', 'my flag', true)
    const url = (fetch as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('my%20group')
    expect(url).toContain('my%20flag')
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 400)
    const err = await create('g', 'x', true).catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(400)
  })
})

describe('enable', () => {
  it('calls POST /group/{group}/toggle/{name}/enable', async () => {
    mockFetch(true)
    await enable('payments', 'checkout')
    expect(fetch).toHaveBeenCalledWith('/group/payments/toggle/checkout/enable', { method: 'POST' })
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    const err = await enable('g', 'x').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(404)
  })
})

describe('disable', () => {
  it('calls POST /group/{group}/toggle/{name}/disable', async () => {
    mockFetch(true)
    await disable('payments', 'checkout')
    expect(fetch).toHaveBeenCalledWith('/group/payments/toggle/checkout/disable', { method: 'POST' })
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    const err = await disable('g', 'x').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(404)
  })
})

describe('remove', () => {
  it('calls DELETE /group/{group}/toggle/{name}', async () => {
    mockFetch(true)
    await remove('payments', 'checkout')
    expect(fetch).toHaveBeenCalledWith('/group/payments/toggle/checkout', { method: 'DELETE' })
  })

  it('throws ApiError with status code when response is not ok', async () => {
    mockFetch(false, undefined, 404)
    const err = await remove('g', 'x').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(404)
  })
})
