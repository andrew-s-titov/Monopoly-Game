import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { BE_ENDPOINT } from "../../config/api";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { getLoggedInUserId } from "../../utils/auth";

interface IBuyProposalProps {
  price: number,
}

const BuyProposalModal = ({ price }: IBuyProposalProps) => {

  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { closeEventModal } = useEventModalContext();
  const { gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUser];

  const payable = playerState.money >= price;
  const closeBuyProposal = () => closeEventModal(ModalId.BUY_PROPOSAL);

  const { put } = useQuery();
  const { execute: buy, isLoading: isBuyLoading } = put({
    url: `${BE_ENDPOINT}/game/buy?action=ACCEPT`,
    onSuccess: closeBuyProposal,
  });
  const { execute: auction, isLoading: isAuctionLoading } = put({
    url: `${BE_ENDPOINT}/game/buy?action=DECLINE`,
    onSuccess: closeBuyProposal,
  });

  return (
    <div className='modal-button-group'>
      <Button
        loading={isBuyLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        disabled={!payable}
        className='modal-button'
        label='Buy'
        severity='success'
        icon='pi pi-money-bill modal-button-icon'
        onClick={() => buy()}
      />
      <Button
        loading={isAuctionLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className='modal-button'
        label='Auction'
        severity='danger'
        icon='pi pi-users modal-button-icon'
        onClick={() => auction()}
      />
    </div>
  );
}

export default memo(BuyProposalModal);
