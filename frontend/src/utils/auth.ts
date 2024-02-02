import { LoginResponse, PlayerAuthData } from "../types/interfaces";

const AUTH_DATA_KEY = "playerData";

export const setAuthData = (loginResponse: LoginResponse) => {
  localStorage.setItem(AUTH_DATA_KEY, JSON.stringify(loginResponse));
}

export const getLoggedInUserId = (): string => {
  return getPlayerAuthData()?.id || 'UNAUTHORIZED';
}

export const getUserAvatar = (): string => {
  return getPlayerAuthData()?.avatar || 'male1';
}

export const isAuthenticated = () => !!getPlayerAuthData();

const getPlayerAuthData = (): PlayerAuthData | undefined => {
  const authData = localStorage.getItem(AUTH_DATA_KEY);
  return authData
    ? JSON.parse(authData) as PlayerAuthData
    : undefined;
}
