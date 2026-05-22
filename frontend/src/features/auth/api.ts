import { apiClient } from '@/lib/axios'

export interface AuthResponse {
  accessToken: string
  refreshToken: string
}

export async function loginDemo(): Promise<AuthResponse> {
  const params = new URLSearchParams()
  params.append('grant_type', 'password')
  params.append('client_id', 'fin-insight-client')
  params.append('username', 'testuser')
  params.append('password', 'test123')
  params.append('scope', 'openid profile email')

  const response = await apiClient.post(
    '/realms/fin-insight/protocol/openid-connect/token',
    params,
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  )

  return {
    accessToken: response.data.access_token,
    refreshToken: response.data.refresh_token,
  }
}

export async function refreshAccessToken(refreshToken: string): Promise<AuthResponse> {
  const params = new URLSearchParams()
  params.append('grant_type', 'refresh_token')
  params.append('client_id', 'fin-insight-client')
  params.append('refresh_token', refreshToken)

  const response = await apiClient.post(
    '/realms/fin-insight/protocol/openid-connect/token',
    params,
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  )

  return {
    accessToken: response.data.access_token,
    refreshToken: response.data.refresh_token,
  }
}
