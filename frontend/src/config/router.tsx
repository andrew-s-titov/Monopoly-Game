import React from 'react';
import { RouteObject } from 'react-router-dom';
import AppMainLayout from "../layouts/AppMainLayout";
import GamePage from "../pages/GamePage";
import LoginPage from "../pages/LoginPage";
import HomePage from "../pages/HomePage";
import AuthenticationLayout from "../layouts/AuthenticationLayout";
import ActiveGameSessionFinderLayout from "../layouts/ActiveGameSessionFinderLayout";
import NotFoundPage from "../pages/NotFoundPage";

export const routerConfig: RouteObject[] = [
  {
    path: '/',
    element: <AppMainLayout/>,
    errorElement: <NotFoundPage/>,
    children: [
      {
        path: '/login',
        element: <LoginPage/>,
      },
      {
        path: '/',
        element: <AuthenticationLayout/>,
        children: [
          {
            path: '/',
            element:
              <ActiveGameSessionFinderLayout>
                <HomePage/>
              </ActiveGameSessionFinderLayout>,
          },
          {
            path: '/game',
            element: <GamePage/>,
          },
        ]
      },
    ],
  },
  {
    path: '/error',
    element: <div>Error page</div>,
  },
];
