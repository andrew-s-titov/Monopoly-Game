import React from 'react';
import { RouteObject } from 'react-router-dom';
import App from "../App";
import StartPage from "../pages/StartPage";
import GamePage from "../pages/GamePage";

export const routerConfig: RouteObject[] = [
  {
    path: '/',
    element: <App/>,
    children: [
      {
        path: '/',
        element: <StartPage/>,
      },
      {
        path: '/game',
        element: <GamePage/>,
      },
    ],
  },
  {
    path: '/error',
    element: <div>Error page</div>,
  },
];
