import StartPageBackground from "./StartPageBackground";
import { useGameState } from "../context/GameStateProvider";

import GameRoomPlayer from "./GameRoomPlayer";
import useQuery from "../hooks/useQuery";
import { BE_ENDPOINT } from "../api/config";
import StartPageButton from "./StartPageButton";

const PreGameRoom = () => {

  const { connectedPlayers } = useGameState();
  const { post } = useQuery();
  const { execute: startGame, isLoading } = post(
    {
      url: `${BE_ENDPOINT}/game`
    });

  const canStartGame = connectedPlayers.length > 1;

  const onStartGameHandler = () => {
    canStartGame && startGame();
  };

  return (
    <StartPageBackground>
      <div className="gr-players-container">
        {connectedPlayers.map(({ id, name, avatar }) =>
          <GameRoomPlayer
            key={id}
            name={name}
            avatar={avatar}
          />
        )}
      </div>
      <StartPageButton
        label='Start the game'
        icon="pi-play"
        isLoading={isLoading}
        isDisabled={!canStartGame}
        onClickHandler={onStartGameHandler}
      />
    </StartPageBackground>
  );
}

export default PreGameRoom;
