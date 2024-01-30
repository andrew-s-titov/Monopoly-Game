import React, { useRef, useState } from "react";
import { Dialog } from "primereact/dialog";
import { ModalId } from "../components/modals";

export interface IModalProps {
  content: React.JSX.Element;
  blurBackground?: boolean;
  isTransparent?: boolean,
  modalId?: ModalId,
  isRounded?: boolean,
}

interface ModalState {
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
                       content,
                       blurBackground = true,
                       isTransparent = false,
                       modalId,
                       isRounded = false,
                     }: IModalProps) => {
    lastModal.current = modalId;
    setModalState(() => ({
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

  const dialogElement =
    <Dialog
      header={modalState.content}
      onHide={closeModal}
      visible={isOpened}
      headerStyle={{
        padding: '0',
        borderRadius: modalState.isRounded ? '2vh' : '0',
      }}
      dismissableMask={isClosable}
      closable={false}
      resizable={false}
      draggable={false}
      modal={modalState.blurBackground}
      className={modalState.isTransparent ? "transparent-dialog" : ""}
      headerClassName={modalState.isTransparent ? "transparent-dialog" : ""}
    >
    </Dialog>;

  return {
    dialogElement,
    openModal,
    closeModal,
  };
};

export default useModal;
