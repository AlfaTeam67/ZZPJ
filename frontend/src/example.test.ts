import { describe, it, expect } from 'vitest'

describe('Example Test Suite', () => {
  it('should pass a simple assertion', () => {
    expect(true).toBe(true)
  })

  it('should work with numbers', () => {
    expect(2 + 2).toBe(4)
  })

  it('should work with strings', () => {
    const greeting = 'Hello, Fin-Insight!'
    expect(greeting).toContain('Fin-Insight')
  })

  it('should work with arrays', () => {
    const arr = [1, 2, 3, 4, 5]
    expect(arr).toHaveLength(5)
    expect(arr).toContain(3)
  })

  it('should work with objects', () => {
    const user = {
      id: '123',
      username: 'testuser',
      email: 'test@example.com',
    }
    expect(user).toHaveProperty('username')
    expect(user.username).toBe('testuser')
  })
})
