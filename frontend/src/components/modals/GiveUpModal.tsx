import { memo } from 'react';

import { Button } from 'primereact/button';
import useQuery from "../../hooks/useQuery";
import { usePopUpModalContext } from "../../context/PopUpModalProvider";
import { useGameState } from "../../context/GameStateProvider";

const GiveUpModal = () => {

  const { gameId } = useGameState();
  const { closePopUpModal } = usePopUpModalContext();
  const { put } = useQuery();
  const { execute: giveUp, isLoading } = put({
    url: `/game/${gameId}/give_up`,
    onSuccess: closePopUpModal,
  });

  return (
    <div className='modal-button-group'>
      <Button
        className='modal-button'
        label='Cancel'
        severity='secondary'
        icon='pi pi-times modal-button-icon'
        onClick={closePopUpModal}
      />
      <Button
        loading={isLoading}
        loadingIcon="pi pi-spin pi-box modal-button-icon"
        className='modal-button'
        label='Give up'
        severity='danger'
        icon='pi pi-flag modal-button-icon'
        onClick={() => giveUp()}
      />
    </div>
  );
}

export default memo(GiveUpModal);
