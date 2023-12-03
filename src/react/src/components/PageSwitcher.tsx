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
        <PopUpModalProvider>
          <EventModalProvider>
            <WebsocketConnectionProvider>
              <Game/>
            </WebsocketConnectionProvider>
          </EventModalProvider>
        </PopUpModalProvider>
      </GameStateProvider>

      : <LoginForm/>
  );
}

export default PageSwitcher;
