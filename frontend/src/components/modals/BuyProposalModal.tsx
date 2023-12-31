import { memo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../api/config";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";

interface IBuyProposalProps {
  playerId: string,
  price: number,
}

const BuyProposalModal = ({ playerId, price }: IBuyProposalProps) => {

  const { closeEventModal } = useEventModalContext();
  const { gameState } = useGameState();
  const playerState = gameState.playerStates[playerId];
  const { get, isLoading } = useQuery();

  const payable = playerState.money >= price;
  const closeBuyProposal = () => closeEventModal(ModalId.BUY_PROPOSAL);

  const onBuyHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/buy?action=ACCEPT`,
      onSuccess: closeBuyProposal,
    });
  };

  const onAuctionHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/buy?action=DECLINE`,
      onSuccess: closeBuyProposal,
    });
  }

  return (
    <div className='modal-button-group'>
      <Button
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        disabled={!payable}
        className='modal-button'
        label='Buy'
        severity='success'
        icon='pi pi-money-bill modal-button-icon'
        onClick={onBuyHandler}
      />
      <Button
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
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
