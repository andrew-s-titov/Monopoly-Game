import { useGameState } from "../context/GameStateProvider";
import GameContent from "./GameContent";
import PreGameRoom from "./PreGameRoom";

const Game = () => {

  const { gameState } = useGameState();

  return (
    gameState.gameStarted
      ? <GameContent/>
      : <PreGameRoom/>
  );
}

export default Game;
