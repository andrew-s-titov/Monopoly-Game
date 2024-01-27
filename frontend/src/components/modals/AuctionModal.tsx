import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { getLoggedInUserId } from "../../utils/auth";

interface IAuctionModalProps {
  proposal: number,
}

const AuctionModal = ({ proposal }: IAuctionModalProps) => {

  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { closeEventModal } = useEventModalContext();
  const { gameId, gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUser];
  const closeAuctionModal = () => closeEventModal(ModalId.AUCTION);

  const { put } = useQuery();
  const { execute: raise, isLoading: isRaiseLoading } = put({
    url: `/game/${gameId}/auction/raise?action=ACCEPT`,
    onSuccess: closeAuctionModal,
  });

  const { execute: decline, isLoading: isDeclineLoading } = put({
    url: `/game/${gameId}/auction/raise?action=DECLINE`,
    onSuccess: closeAuctionModal,
  });

  const payable = playerState.money >= proposal;

  return (
    <div className='modal-button-group'>
      <Button
        loading={isRaiseLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        disabled={!payable}
        className="modal-button"
        label='Raise'
        severity="success"
        icon="pi pi-sort-amount-up modal-button-icon"
        onClick={() => raise()}
      />
      <Button
        loading={isDeclineLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className="modal-button"
        severity="secondary"
        label='Decline'
        icon="pi pi-times modal-button-icon"
        onClick={() => decline()}
      />
    </div>
  );
}

export default memo(AuctionModal);
