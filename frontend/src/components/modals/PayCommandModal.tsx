import { memo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../api/config";

interface IPayCommandModalProps {
  playerId: string;
  sum: number;
  wiseToGiveUp: boolean;
}

const PayCommandModal = ({ playerId, sum, wiseToGiveUp }: IPayCommandModalProps) => {

  const { gameState } = useGameState();
  const playerState = gameState.playerStates[playerId];
  const { get } = useQuery();

  const payable = playerState.money >= sum;

  const onPayHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/pay`,
    });
  };

  const onGiveUpHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/player/give_up`,
    });
  };

  return (
    <div className='modal-button-group'>
      <Button
        disabled={!payable}
        className="modal-button"
        label='Pay'
        severity="secondary"
        icon="pi pi-money-bill modal-button-icon"
        onClick={onPayHandler}
      />
      {wiseToGiveUp &&
        <Button
          className="modal-button"
          severity="danger"
          label='Give up'
          icon="pi pi-flag modal-button-icon"
          onClick={onGiveUpHandler}
        />
      }
    </div>
  );
}

export default memo(PayCommandModal);
