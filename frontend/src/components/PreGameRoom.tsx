import StartPageBackground from "./StartPageBackground";
import { useGameState } from "../context/GameStateProvider";

import GameRoomPlayer from "./GameRoomPlayer";
import useQuery from "../hooks/useQuery";
import StartPageButton from "./StartPageButton";
import StartPageCenteredContent from "./StartPageCenteredContent";
import { useTranslations } from "../i18n/config";

const PreGameRoom = () => {

  const { t } = useTranslations();
  const { gameId, connectedPlayers } = useGameState();
  const { post } = useQuery();
  const { execute: startGame, isLoading } = post(
    {
      url: `/game/${gameId}`,
    });

  const canStartGame = connectedPlayers.length > 1;

  const onStartGameHandler = () => {
    canStartGame && startGame();
  };

  return (
    <StartPageBackground>
      <StartPageCenteredContent>
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
          label={t('action.startGame')}
          icon="pi-play"
          isLoading={isLoading}
          isDisabled={!canStartGame}
          onClickHandler={onStartGameHandler}
        />
      </StartPageCenteredContent>
    </StartPageBackground>
  );
}

export default PreGameRoom;
