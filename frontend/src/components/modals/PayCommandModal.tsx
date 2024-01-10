import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../config/api";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { getLoggedInUserId } from "../../utils/auth";

interface IPayCommandModalProps {
  sum: number;
  wiseToGiveUp: boolean;
}

const PayCommandModal = ({ sum, wiseToGiveUp }: IPayCommandModalProps) => {

  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { closeEventModal } = useEventModalContext();
  const { gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUser];

  const payable = playerState.money >= sum;
  const closePayCommand = () => closeEventModal(ModalId.PAY_COMMAND);
  const { put } = useQuery();
  const { execute: pay, isLoading: isPayLoading } = put({
    url: `${BE_ENDPOINT}/game/pay`,
    onSuccess: closePayCommand,
  });
  const { execute: giveUp, isLoading: isGiveUpLoading } = put({
    url: `${BE_ENDPOINT}/game/player/give_up`,
    onSuccess: closePayCommand,
  });

  return (
    <div className='modal-button-group'>
      <Button
        disabled={!payable}
        loading={isPayLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className="modal-button"
        label='Pay'
        severity="secondary"
        icon="pi pi-money-bill modal-button-icon"
        onClick={() => pay()}
      />
      {wiseToGiveUp &&
        <Button
          className="modal-button"
          loading={isGiveUpLoading}
          loadingIcon="pi pi-spin pi-box modal-button-icon"
          severity="danger"
          label='Give up'
          icon="pi pi-flag modal-button-icon"
          onClick={() => giveUp()}
        />
      }
    </div>
  );
}

export default memo(PayCommandModal);
