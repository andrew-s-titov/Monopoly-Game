import { memo } from "react";

import { Button } from "primereact/button";
import { useEventModalContext } from "../../context/EventModalProvider";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../api/config";

interface IAuctionModalProps {
  playerId: string,
  proposal: number,
}

const AuctionModal = ({ playerId, proposal }: IAuctionModalProps) => {

  const { gameState } = useGameState();
  const playerState = gameState.playerStates[playerId];
  const { closeEventModal } = useEventModalContext();
  const { get, isLoading } = useQuery();

  const payable = playerState.money >= proposal;

  const onRaiseHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/auction/raise?action=ACCEPT`,
      onSuccess: closeEventModal,
    });
  };

  const onDeclineHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/auction/raise?action=DECLINE`,
      onSuccess: closeEventModal,
    });
  };

  return (
    <div className='modal-button-group'>
      <Button
        loading={isLoading}
        disabled={!payable}
        className="modal-button"
        label='Raise'
        severity="success"
        icon="pi pi-sort-amount-up modal-button-icon"
        onClick={onRaiseHandler}
      />
      <Button
        loading={isLoading}
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
