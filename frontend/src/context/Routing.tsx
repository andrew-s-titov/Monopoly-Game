import { createContext, PropsWithChildren, useContext, useEffect, useState } from "react";

import { BE_ENDPOINT } from "../config/api";
import { useAuthContext } from "./AuthContextProvider";
import useQuery from "../hooks/useQuery";
import LoginPage from "../pages/LoginPage";
import GamePage from "../pages/GamePage";
import HomePage from "../pages/HomePage";

export type AppPage = 'home' | 'game';

interface IRoutingContext {
  navigate: (page: Exclude<AppPage, 'game'>) => void,
  navigateToGame: (gameId: string) => void,
}

const RoutingContext = createContext({} as IRoutingContext);

export const PageRoutingProvider = () => {

  const { isLoggedIn } = useAuthContext();
  const [activeGameId, setActiveGameId] = useState<string>('');
  const [activePage, setActivePage] = useState<AppPage>('home');

  const navigate = (page: Exclude<AppPage, 'game'>) => setActivePage(page);
  const navigateToGame = (gameId: string) => {
    setActiveGameId(gameId);
    setActivePage('game');
  }

  const { get } = useQuery();
  const { execute: findSession } = get({
    url: `${BE_ENDPOINT}/user`,
    responseHandler: ({ gameId }) => {
      if (gameId) {
        setActiveGameId(gameId);
        setActivePage('game');
      }
    },
  });

  useEffect(() => {
      isLoggedIn && !activeGameId && findSession();
    },
    [activeGameId]);

  return (
    <RoutingContext.Provider value={{ navigate, navigateToGame }}>
      {!isLoggedIn ?
        <LoginPage/>
        :
        <>
          {activePage === 'game' && <GamePage gameId={activeGameId}/>}
          {activePage === 'home' && <HomePage/>}
        </>
      }
    </RoutingContext.Provider>

  );
}

export const useRouting = () => useContext(RoutingContext);

interface INavigatorLinkProps extends PropsWithChildren {
  to: Exclude<AppPage, 'game'>,
}

export const NavigatorLink = ({children, to}: INavigatorLinkProps) => {

  const { navigate} = useRouting();

  return (
    <div
      className="navigator-link"
      onClick={() => navigate(to)}
    >
      <a>{children}</a>
    </div>
  );
}
