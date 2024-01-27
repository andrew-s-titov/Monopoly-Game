import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { getLoggedInUserId } from "../../utils/auth";
import { useTranslations } from "../../i18n/config";

interface IBuyProposalProps {
  price: number,
}

const BuyProposalModal = ({ price }: IBuyProposalProps) => {

  const { t } = useTranslations();
  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { closeEventModal } = useEventModalContext();
  const { gameId, gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUser];

  const payable = playerState.money >= price;
  const closeBuyProposal = () => closeEventModal(ModalId.BUY_PROPOSAL);

  const { put } = useQuery();
  const { execute: buy, isLoading: isBuyLoading } = put({
    url: `/game/${gameId}/buy?action=ACCEPT`,
    onSuccess: closeBuyProposal,
  });
  const { execute: auction, isLoading: isAuctionLoading } = put({
    url: `/game/${gameId}/buy?action=DECLINE`,
    onSuccess: closeBuyProposal,
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
        loading={isAuctionLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className='modal-button'
        label={t('action.auction')}
        severity='danger'
        icon='pi pi-users modal-button-icon'
        onClick={() => auction()}
      />
    </div>
  );
}

export default memo(BuyProposalModal);
