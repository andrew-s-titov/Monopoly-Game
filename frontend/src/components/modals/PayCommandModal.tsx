import { memo, useMemo } from "react";

import { Button } from "primereact/button";
import useQuery from "../../hooks/useQuery";
import { useGameState } from "../../context/GameStateProvider";
import { useEventModalContext } from "../../context/EventModalProvider";
import { ModalId } from "./index";
import { getLoggedInUserId } from "../../utils/auth";
import { useTranslations } from "../../i18n/config";

interface IPayCommandModalProps {
  sum: number;
  wiseToGiveUp: boolean;
}

const PayCommandModal = ({ sum, wiseToGiveUp }: IPayCommandModalProps) => {

  const { t } = useTranslations();
  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { closeEventModal } = useEventModalContext();
  const { gameId, gameState } = useGameState();
  const playerState = gameState.playerStates[loggedInUser];

  const payable = playerState.money >= sum;
  const closePayCommand = () => closeEventModal(ModalId.PAY_COMMAND);
  const { put } = useQuery();
  const { execute: pay, isLoading: isPayLoading } = put({
    url: `/game/${gameId}/pay`,
    onSuccess: closePayCommand,
  });
  const { execute: giveUp, isLoading: isGiveUpLoading } = put({
    url: `/game/${gameId}/give_up`,
    onSuccess: closePayCommand,
  });

  return (
    <div className='modal-content'>
      <div className='modal-title'>
        {t('modal.pay', { amount: sum })}
      </div>
      <div className='modal-button-group'>
        <Button
          disabled={!payable}
          loading={isPayLoading}
          loadingIcon="pi pi-spin pi-box modal-button-icon"
          className="modal-button"
          label={t('action.pay')}
          severity="secondary"
          icon="pi pi-money-bill modal-button-icon"
          onClick={() => pay()}
        />
        {wiseToGiveUp &&
          <Button
            className="modal-button"
            loading={isGiveUpLoading}
            loadingIcon="pi pi-spin pi-box modal-button-icon"
            severity="danger"
            label={t('action.giveUp')}
            icon="pi pi-flag modal-button-icon"
            onClick={() => giveUp()}
          />
        }
      </div>
    </div>
  );
}

export default memo(PayCommandModal);
