import { getLoggedInUserId, getLoggedInUserName, PLAYER_ID_KEY, PLAYER_NAME_KEY } from "../utils/auth";

export const BE_ENDPOINT = 'https://742e-109-243-0-109.ngrok-free.app';

export const getWebsocketUrl = () => {
  return `${getWebsocketEndpoint()}?${PLAYER_ID_KEY}=${getLoggedInUserId()}&${PLAYER_NAME_KEY}=${getLoggedInUserName()}`;
}

const getWebsocketEndpoint = () => {
  const host = BE_ENDPOINT.split('://')[1];
  return `${BE_ENDPOINT.startsWith('https') ? 'wss' : 'ws'}://${host}/ws`;
}
