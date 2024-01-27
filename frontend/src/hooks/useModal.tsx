import React, { useRef, useState } from "react";
import { Dialog } from "primereact/dialog";
import { ModalId } from "../components/modals";

export interface IModalProps {
  header: React.JSX.Element;
  content?: React.JSX.Element;
  blurBackground?: boolean;
  isTransparent?: boolean,
  modalId?: ModalId,
  isRounded?: boolean,
}

interface ModalState {
  header?: React.JSX.Element,
  content?: React.JSX.Element,
  blurBackground: boolean,
  isTransparent: boolean,
  isOpened: boolean,
  isRounded: boolean,
}

const useModal = (isClosable?: boolean) => {
  const lastModal = useRef<ModalId>();
  const [modalState, setModalState] = useState<ModalState>(() => ({
    isOpened: false,
    isTransparent: false,
    blurBackground: true,
    isRounded: false,
  }));
  const [isOpened, setIsOpened] = useState<boolean>();

  const openModal = ({
                       header,
                       content,
                       blurBackground = true,
                       isTransparent = false,
                       modalId,
                       isRounded = false,
                     }: IModalProps) => {
    lastModal.current = modalId;
    setModalState(() => ({
      header,
      content,
      blurBackground,
      isTransparent,
      isRounded,
      isOpened: true,
    }));
    setIsOpened(true);
  };

  const closeModal = (modalId?: ModalId) => {
    if (modalId && modalId !== lastModal.current) {
      return;
    }
    setIsOpened(false);
  };

  const headerWrapper = modalState.content
    ? <div className='modal-header'>{modalState.header}</div>
    : modalState.header;

  const dialogElement =
    <Dialog
      header={headerWrapper}
      onHide={closeModal}
      visible={isOpened}
      headerStyle={{
        padding: '0',
        borderTopLeftRadius: modalState.isRounded ? '2vh' : '0',
        borderTopRightRadius: modalState.isRounded ? '2vh' : '0',
        borderBottomLeftRadius: modalState.isRounded && !modalState.content ? '2vh' : '0',
        borderBottomRightRadius: modalState.isRounded && !modalState.content ? '2vh' : '0',
    }}
      contentStyle={{
        padding: '0',
        borderBottomLeftRadius: modalState.isRounded ? '2hv' : '0',
        borderBottomRightRadius: modalState.isRounded ? '2hv' : '0',
    }}
      dismissableMask={isClosable}
      closable={false}
      resizable={false}
      draggable={false}
      modal={modalState.blurBackground}
      className={modalState.isTransparent ? "transparent-dialog" : ""}
      headerClassName={modalState.isTransparent ? "transparent-dialog" : ""}
    >
      {modalState.content &&
        <div className='modal-content'>
          {modalState.content}
        </div>
      }
    </Dialog>;

  return {
    dialogElement,
    openModal,
    closeModal,
  };
};

export default useModal;
