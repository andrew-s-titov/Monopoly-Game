import LoginForm from "./LoginForm";
import { GameStateProvider } from "../context/GameStateProvider";
import { useAuthContext } from "../context/AuthContextProvider";
import Game from "./Game";
import { EventModalProvider } from "../context/EventModalProvider";
import WebsocketConnectionProvider from "../context/WebsocketConnectionProvider";
import { PopUpModalProvider } from "../context/PopUpModalProvider";

const PageSwitcher = () => {

  const { isLoggedIn } = useAuthContext();

  return (
    isLoggedIn
      ?
      <GameStateProvider>
        <EventModalProvider>
          <PopUpModalProvider>
            <WebsocketConnectionProvider>
              <Game/>
            </WebsocketConnectionProvider>
          </PopUpModalProvider>
        </EventModalProvider>
      </GameStateProvider>

      : <LoginForm/>
  );
}

export default PageSwitcher;
