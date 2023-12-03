import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { BE_ENDPOINT } from "../../api/config";
import { useGameState } from "../../context/GameStateProvider";
import { getLoggedInUserId } from "../../utils/auth";

interface IAuctionBuyProposalProps {
  proposal: number,
}

const AuctionBuyProposalModal = ({ proposal }: IAuctionBuyProposalProps) => {

  const loggedInUserId = useMemo(getLoggedInUserId, []);
  const { gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUserId];
  const { get, isLoading } = useQuery();

  const payable = playerState.money >= proposal;

  const onBuyHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/auction/buy?action=ACCEPT`,
    });
  };

  const onDeclineHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/auction/buy?action=DECLINE`,
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
        label='Decline'
        severity='secondary'
        icon='pi pi-times modal-button-icon'
        onClick={onDeclineHandler}
      />
    </div>
  );
}

export default memo(AuctionBuyProposalModal);
