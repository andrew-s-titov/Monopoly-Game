import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { getLoggedInUserId } from "../../utils/auth";

const JailReleaseModal = () => {

  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { closeEventModal } = useEventModalContext();
  const { gameId, gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUser];

  const payable = playerState.money >= 50;
  const closeJailRelease = () => closeEventModal(ModalId.JAIL_RELEASE);

  const { put } = useQuery();
  const { execute: pay, isLoading: isPayLoading } = put({
    url: `/game/${gameId}/jail?action=PAY`,
    onSuccess: closeJailRelease,
  });
  const { execute: tryLuck, isLoading: isLuckLoading } = put({
    url: `/game/${gameId}/jail?action=LUCK`,
    onSuccess: closeJailRelease,
  });

  return (
    <div className='modal-button-group'>
      <Button
        loading={isPayLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        disabled={!payable}
        className='modal-button'
        label='Pay $50'
        severity='success'
        icon='pi pi-money-bill modal-button-icon'
        onClick={() => pay()}
      />
      <Button
        loading={isLuckLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className='modal-button'
        label='Try luck'
        icon='pi pi-box modal-button-icon'
        onClick={() => tryLuck()}
      />
    </div>
  );
}

export default memo(JailReleaseModal);
