import { getLoggedInUserId, getLoggedInUserName, PLAYER_ID_KEY, PLAYER_NAME_KEY } from "../utils/auth";

// local dev endpoint
export const BE_ENDPOINT = 'http://localhost:8080';
// export const BE_ENDPOINT = document.location.origin;

export const getWebsocketUrl = () => {
  const wsUri = getWebsocketEndpoint();
  const userId = getLoggedInUserId();
  const userName = getLoggedInUserName();
  return `${wsUri}?${PLAYER_ID_KEY}=${userId}&${PLAYER_NAME_KEY}=${userName}`;
}

const getWebsocketEndpoint = () => {
  const host = BE_ENDPOINT.split('://')[1];
  return `${BE_ENDPOINT.startsWith('https') ? 'wss' : 'ws'}://${host}/ws`;
}
