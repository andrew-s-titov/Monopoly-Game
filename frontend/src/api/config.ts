import { websocketRequestParams } from "../utils/auth";

// local dev endpoint
// export const BE_ENDPOINT = 'http://localhost:8080';
export const BE_ENDPOINT = document.location.origin;

export const getWebsocketUrl = () => {
  const wsUri = getWebsocketEndpoint();
  return `${wsUri}?${websocketRequestParams()}`;
}

const getWebsocketEndpoint = () => {
  const host = BE_ENDPOINT.split('://')[1];
  return `${BE_ENDPOINT.startsWith('https') ? 'wss' : 'ws'}://${host}/ws`;
}
