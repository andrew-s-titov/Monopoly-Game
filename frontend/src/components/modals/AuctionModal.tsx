import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../api/config";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { getLoggedInUserId } from "../../utils/auth";

interface IAuctionModalProps {
  proposal: number,
}

const AuctionModal = ({ proposal }: IAuctionModalProps) => {

  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { closeEventModal } = useEventModalContext();
  const { gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUser];
  const { get, isLoading } = useQuery();

  const payable = playerState.money >= proposal;
  const closeAuctionModal = () => closeEventModal(ModalId.AUCTION);

  const onRaiseHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/auction/raise?action=ACCEPT`,
      onSuccess: closeAuctionModal,
    });
  };

  const onDeclineHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/auction/raise?action=DECLINE`,
      onSuccess: closeAuctionModal,
    });
  };

  return (
    <div className='modal-button-group'>
      <Button
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        disabled={!payable}
        className="modal-button"
        label='Raise'
        severity="success"
        icon="pi pi-sort-amount-up modal-button-icon"
        onClick={onRaiseHandler}
      />
      <Button
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className="modal-button"
        severity="secondary"
        label='Decline'
        icon="pi pi-times modal-button-icon"
        onClick={onDeclineHandler}
      />
    </div>
  );
}

export default memo(AuctionModal);
