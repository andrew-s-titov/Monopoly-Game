import { EventModalProvider } from "../context/EventModalProvider";
import { PopUpModalProvider } from "../context/PopUpModalProvider";
import WebsocketConnectionProvider from "../context/WebsocketConnectionProvider";
import Game from "../components/Game";
import { GameStateProvider } from "../context/GameStateProvider";

const GamePage = () => {
  return (
    <GameStateProvider>
      <EventModalProvider>
        <PopUpModalProvider>
          <WebsocketConnectionProvider>
            <Game/>
          </WebsocketConnectionProvider>
        </PopUpModalProvider>
      </EventModalProvider>
    </GameStateProvider>
  );
}

export default GamePage;
