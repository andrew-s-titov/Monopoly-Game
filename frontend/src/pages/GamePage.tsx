import { EventModalProvider } from "../context/EventModalProvider";
import { PopUpModalProvider } from "../context/PopUpModalProvider";
import WebsocketConnectionProvider from "../context/WebsocketConnectionProvider";
import Game from "../components/Game";
import { GameStateProvider } from "../context/GameStateProvider";
import { useParams } from "react-router-dom";

const GamePage = () => {

  const gameId = useParams()['gameId'] || '';

  return (
    <GameStateProvider gameId={gameId}>
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
