import { memo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../api/config";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";

interface IPayCommandModalProps {
  playerId: string;
  sum: number;
  wiseToGiveUp: boolean;
}

const PayCommandModal = ({ playerId, sum, wiseToGiveUp }: IPayCommandModalProps) => {

  const { closeEventModal } = useEventModalContext();
  const { gameState } = useGameState();
  const playerState = gameState.playerStates[playerId];
  const { get, isLoading } = useQuery();

  const payable = playerState.money >= sum;
  const closePayCommand = () => closeEventModal(ModalId.PAY_COMMAND);

  const onPayHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/pay`,
      onSuccess: closePayCommand,
    });
  };

  const onGiveUpHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/player/give_up`,
      onSuccess: closePayCommand,
    });
  };

  return (
    <div className='modal-button-group'>
      <Button
        disabled={!payable}
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className="modal-button"
        label='Pay'
        severity="secondary"
        icon="pi pi-money-bill modal-button-icon"
        onClick={onPayHandler}
      />
      {wiseToGiveUp &&
        <Button
          className="modal-button"
          loading={isLoading}
          loadingIcon="pi pi-spin pi-box modal-button-icon"
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
