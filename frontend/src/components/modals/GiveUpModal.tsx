import { memo } from 'react';

import { Button } from 'primereact/button';
import useQuery from "../../hooks/useQuery";
import { usePopUpModalContext } from "../../context/PopUpModalProvider";
import { BE_ENDPOINT } from "../../api/config";

const GiveUpModal = () => {

  const { closePopUpModal } = usePopUpModalContext();
  const { get, isLoading } = useQuery();

  const onGiveUpHandler = () => {
    get({
      url: `${BE_ENDPOINT}/game/player/give_up`,
      onSuccess: closePopUpModal,
    });
  };

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
        className='modal-button'
        label='Give up'
        severity='danger'
        icon='pi pi-flag modal-button-icon'
        onClick={onGiveUpHandler}
      />
    </div>
  );
}

export default memo(GiveUpModal);
