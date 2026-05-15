import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

export interface AuthUser {
  id: string
  username?: string
  email?: string
  firstName?: string
  lastName?: string
  roles: string[]
}

interface AuthState {
  initialized: boolean
  initError: string | null
  token: string | null
  user: AuthUser | null
}

const initialState: AuthState = {
  initialized: false,
  initError: null,
  token: null,
  user: null,
}

interface AuthSession {
  token: string
  user: AuthUser
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    /** Backwards-compatible setter still used by tests / demo flow. */
    setToken(state, action: PayloadAction<string | null>) {
      state.token = action.payload
      if (action.payload === null) {
        state.user = null
      }
      state.initialized = true
    },
    authReady(state, action: PayloadAction<AuthSession | null>) {
      state.initialized = true
      state.initError = null
      if (action.payload === null) {
        state.token = null
        state.user = null
      } else {
        state.token = action.payload.token
        state.user = action.payload.user
      }
    },
    authInitFailed(state, action: PayloadAction<string>) {
      state.initialized = true
      state.initError = action.payload
      state.token = null
      state.user = null
    },
    tokenRefreshed(state, action: PayloadAction<string>) {
      state.token = action.payload
    },
    signedOut(state) {
      state.token = null
      state.user = null
    },
  },
})

export const { setToken, authReady, authInitFailed, tokenRefreshed, signedOut } = authSlice.actions
export const authReducer = authSlice.reducer
