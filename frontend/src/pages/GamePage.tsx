import { EventModalProvider } from "../context/EventModalProvider";
import { PopUpModalProvider } from "../context/PopUpModalProvider";
import ActiveGameContextProvider from "../context/ActiveGameContextProvider";
import Game from "../components/Game";
import { GameStateProvider } from "../context/GameStateProvider";

interface IGamePageProps {
  gameId: string,
}

const GamePage = ({ gameId }: IGamePageProps) => {

  return (
    <GameStateProvider gameId={gameId}>
      <EventModalProvider>
        <PopUpModalProvider>
          <ActiveGameContextProvider>
            <Game/>
          </ActiveGameContextProvider>
        </PopUpModalProvider>
      </EventModalProvider>
    </GameStateProvider>
  );
}

export default GamePage;
