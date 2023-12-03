import { useGameState } from "../context/GameStateProvider";
import PlayerView from "./PlayerView";

const PlayerContainer = () => {

  const {gameState} = useGameState();

  return (
    <div className='player-container'>
      {Object.keys(gameState.playerStates).map((playerId) => (
        <PlayerView
          playerId={playerId}
          key={playerId}
        />)
      )}
    </div>
  );
}

export default PlayerContainer;