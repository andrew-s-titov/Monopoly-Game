import StartPageBackground from "./StartPageBackground";
import { useGameState } from "../context/GameStateProvider";

import GameRoomPlayer from "./GameRoomPlayer";
import useQuery from "../hooks/useQuery";
import { BE_ENDPOINT } from "../api/config";
import StartPageButton from "./StartPageButton";

const PreGameRoom = () => {

  const { connectedPlayers } = useGameState();
  const { post, isLoading } = useQuery();

  const canStartGame = connectedPlayers.length > 1;

  const onStartGameHandler = () => {
    canStartGame && post(
      {
        url: `${BE_ENDPOINT}/game`
      });
  };

  return (
    <StartPageBackground>
      <div className="gr-players-container">
        {connectedPlayers.map(({ playerId, name }) =>
          <GameRoomPlayer
            key={playerId}
            name={name}
          />
        )}
      </div>
      <StartPageButton
        icon="pi-play"
        isLoading={isLoading}
        isDisabled={!canStartGame}
        onClickHandler={onStartGameHandler}
      />
    </StartPageBackground>
  );
}

export default PreGameRoom;
