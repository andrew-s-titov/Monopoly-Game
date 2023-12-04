import { memo } from "react";

import { Button } from "primereact/button";
import { useEventModalContext } from "../../context/EventModalProvider";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../api/config";

interface IBuyProposalProps {
  playerId: string,
  price: number,
}

const BuyProposalModal = ({ playerId, price }: IBuyProposalProps) => {

  const { gameState } = useGameState();
  const playerState = gameState.playerStates[playerId];
  const { closeEventModal } = useEventModalContext();
  const { get, isLoading } = useQuery();

  const payable = playerState.money >= price;

  const onBuyHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/buy?action=ACCEPT`,
    });
  };

  const onAuctionHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/buy?action=DECLINE`,
      onSuccess: closeEventModal,
    });
  }

  return (
    <div className='modal-button-group'>
      <Button
        loading={isLoading}
        disabled={!payable}
        className='modal-button'
        label='Buy'
        severity='success'
        icon='pi pi-money-bill modal-button-icon'
        onClick={onBuyHandler}
      />
      <Button
        loading={isLoading}
        className='modal-button'
        label='Auction'
        severity='danger'
        icon='pi pi-users modal-button-icon'
        onClick={onAuctionHandler}
      />
    </div>
  );
}

export default memo(BuyProposalModal);
