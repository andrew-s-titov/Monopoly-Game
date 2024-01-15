import { getLoggedInUserId } from "../utils/auth";

// local dev endpoint
export const BE_ENDPOINT = 'http://localhost:8080';
// export const BE_ENDPOINT = document.location.origin;

const host = BE_ENDPOINT.split('://')[1];
const wsEndpoint = `${BE_ENDPOINT.startsWith('https') ? 'wss' : 'ws'}://${host}`;

export const getLandingPageWebsocketUrl = () => `${wsEndpoint}/start/${getLoggedInUserId()}`;
export const getGameWebsocketUrl = (gameId: string) => `${wsEndpoint}/game/${gameId}/${getLoggedInUserId()}`;

export const gameBaseUrl = (gameId: string) => `${BE_ENDPOINT}/game/${gameId}`;
