import { memo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../api/config";

interface IJailReleaseModalProps {
  playerId: string,
}

const JailReleaseModal = ({ playerId }: IJailReleaseModalProps) => {

  const { gameState } = useGameState();
  const playerState = gameState.playerStates[playerId];
  const { get, isLoading } = useQuery();

  const payable = playerState.money >= 50;

  const onPayHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/jail?action=PAY`,
    });
  };

  const onTryLuckHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/jail?action=LUCK`,
    });
  };

  return (
    <div className='modal-button-group'>
      <Button
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        disabled={!payable}
        className='modal-button'
        label='Pay $50'
        severity='success'
        icon='pi pi-money-bill modal-button-icon'
        onClick={onPayHandler}
      />
      <Button
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className='modal-button'
        label='Try luck'
        icon='pi pi-box modal-button-icon'
        onClick={onTryLuckHandler}
      />
    </div>
  );
}

export default memo(JailReleaseModal);
