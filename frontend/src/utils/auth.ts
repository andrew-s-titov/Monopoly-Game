import { LoginData, PlayerAuthData } from "../types/interfaces";

export const PLAYER_ID_KEY = 'player_id';

const PLAYER_NAME_KEY = 'player_name';
const PLAYER_AVATAR_KEY = 'player_avatar';
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

export const websocketRequestParams = (): string => {
  const authData = getPlayerAuthData();
  return authData
    ? `${PLAYER_ID_KEY}=${authData.id}&${PLAYER_NAME_KEY}=${authData.name}&${PLAYER_AVATAR_KEY}=${authData.avatar}`
    : '';
}

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

