import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

interface AuthState {
  token: string | null
  refreshToken: string | null
}

const initialState: AuthState = {
  token: null,
  refreshToken: null,
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setAuth(state, action: PayloadAction<{ token: string | null; refreshToken: string | null }>) {
      state.token = action.payload.token
      state.refreshToken = action.payload.refreshToken
    },
    logout(state) {
      state.token = null
      state.refreshToken = null
    },
  },
})

export const { setAuth, logout } = authSlice.actions
export const authReducer = authSlice.reducer
