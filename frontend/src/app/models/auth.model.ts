export interface User {
  username: string;
  profile: string;
  permissions: string[];
  token?: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  profile: string;
  permissions: string[];
}
