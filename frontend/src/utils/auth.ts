import { LoginData, PlayerAuthData } from "../types/interfaces";

const AUTH_DATA_KEY = "playerData";

export const setAuthData = (loginData: LoginData, id: string) => {
  localStorage.setItem(AUTH_DATA_KEY, JSON.stringify(constructAuthData(loginData, id)));
}

export const getLoggedInUserId = (): string => {
  const authData = getPlayerAuthData();
  return authData
    ? authData.id
    : 'UNAUTHORIZED';
}

export const isAuthenticated = () => !!getPlayerAuthData();

const getPlayerAuthData = (): PlayerAuthData | undefined => {
  const authData = localStorage.getItem(AUTH_DATA_KEY);
  return authData
    ? JSON.parse(authData) as PlayerAuthData
    : undefined;
}

const constructAuthData = ({ name, avatar }: LoginData, id: string): PlayerAuthData => {
  return {
    id,
    name,
    avatar,
  };
}

