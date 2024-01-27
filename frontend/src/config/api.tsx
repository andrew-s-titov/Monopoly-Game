import { getLoggedInUserId } from "../utils/auth";

// export const BE_ENDPOINT = 'http://localhost:8080'; // local dev endpoint
export const BE_ENDPOINT = document.location.origin; // when serving FE & BE from the same server

const host = BE_ENDPOINT.split('://')[1];
const wsEndpoint = `${BE_ENDPOINT.startsWith('https') ? 'wss' : 'ws'}://${host}`;

export const getLandingPageWebsocketUrl = () => `${wsEndpoint}/start/${getLoggedInUserId()}`;
export const getGameWebsocketUrl = (gameId: string) => `${wsEndpoint}/game/${gameId}/${getLoggedInUserId()}`;
