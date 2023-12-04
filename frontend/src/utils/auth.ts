import { AuthData } from "../types/interfaces";

export const PLAYER_ID_KEY = 'player_id';
export const PLAYER_NAME_KEY = 'player_name';

export const setAuthData = ({ player_id, player_name }: AuthData) => {
  localStorage.setItem(PLAYER_ID_KEY, player_id);
  localStorage.setItem(PLAYER_NAME_KEY, player_name);
}

export const getLoggedInUserId = (): string => {
  const playerId = localStorage.getItem(PLAYER_ID_KEY);
  return playerId
    ? playerId
    : 'UNAUTHORIZED';
}

export const getLoggedInUserName = (): string => {
  const playerName = localStorage.getItem(PLAYER_NAME_KEY);
  return playerName
    ? playerName
    : 'NONAME';
}

export const isAuthenticated = () => {
  return !!localStorage.getItem(PLAYER_ID_KEY) && !!localStorage.getItem(PLAYER_NAME_KEY);
}
