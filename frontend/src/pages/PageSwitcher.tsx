import { useEffect } from "react";

import { useQuery, useStateSelector, useStateDispatch } from "../hooks";
import { LoginPage, GamePage, HomePage } from "./index";
import { AppPage, navigateToGame } from "../store/slice/navigation";

const PageSwitcher = () => {
  const isAuthenticated = useStateSelector(state => state.auth.isLoggedIn);
  const currentPage = useStateSelector(state => state.navigation.currentPage);
  const currentGameId = useStateSelector(state => state.navigation.currentGameId);
  const dispatch = useStateDispatch();
  const navigateToCurrentGame = (gameId: string) => dispatch(navigateToGame(gameId));

  const { get } = useQuery();
  const { execute: findSession } = get({
    url: `/user`,
    responseHandler: ({ gameId }) => {
      gameId && navigateToCurrentGame(gameId);
    },
  });

  useEffect(() => {
      isAuthenticated && !currentGameId && findSession();
    },
    [ currentGameId ]);

  return (
    !isAuthenticated ?
      <LoginPage/>
      :
      <>
        {currentPage === AppPage.GAME && currentGameId && <GamePage gameId={currentGameId}/>}
        {currentPage === AppPage.HOME && <HomePage/>}
      </>
  );
}

export default PageSwitcher;
