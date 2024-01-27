import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { getLoggedInUserId } from "../../utils/auth";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { useTranslations } from "../../i18n/config";

interface IAuctionBuyProposalProps {
  proposal: number,
}

const AuctionBuyProposalModal = ({ proposal }: IAuctionBuyProposalProps) => {

  const { t } = useTranslations();
  const { closeEventModal } = useEventModalContext();
  const loggedInUserId = useMemo(getLoggedInUserId, []);
  const { gameId, gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUserId];

  const payable = playerState.money >= proposal;
  const closeAuctionBuyProposal = () => closeEventModal(ModalId.AUCTION_BUY_PROPOSAL);

  const { put } = useQuery();
  const { execute: buy, isLoading: isBuyLoading } = put({
    url: `/game/${gameId}/auction/buy?action=ACCEPT`,
    onSuccess: closeAuctionBuyProposal,
  });
  const { execute: decline, isLoading: isDeclineLoading } = put({
    url: `/game/${gameId}/auction/buy?action=DECLINE`,
    onSuccess: closeAuctionBuyProposal,
  });

  return (
    <div className='modal-button-group'>
      <Button
        loading={isBuyLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        disabled={!payable}
        className='modal-button'
        label={t('action.buy')}
        severity='success'
        icon='pi pi-money-bill modal-button-icon'
        onClick={() => buy()}
      />
      <Button
        loading={isDeclineLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className='modal-button'
        label={t('action.decline')}
        severity='secondary'
        icon='pi pi-times modal-button-icon'
        onClick={() => decline()}
      />
    </div>
  );
}

export default memo(AuctionBuyProposalModal);
