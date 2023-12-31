import React, { useRef, useState } from "react";
import { Dialog } from "primereact/dialog";
import { ModalId } from "../components/modals";

export interface IModalProps {
  header: React.JSX.Element;
  modalContent?: React.JSX.Element;
  modal?: boolean;
  transparent?: boolean,
  modalId?: ModalId,
}

const useModal = (isClosable?: boolean) => {
  const lastModal = useRef<ModalId>();
  const [modalContent, setModalContent] = useState<React.JSX.Element>();
  const [modalHeader, setModalHeader] = useState<React.JSX.Element>();
  const [isOpened, setIsOpened] = useState(false);
  const [isModal, setIsModal] = useState(true);
  const [isTransparent, setIsTransparent] = useState(false);

  const openModal = ({
                       header,
                       modalContent,
                       modal = true,
                       transparent = false,
                       modalId
                     }: IModalProps) => {
    lastModal.current = modalId;
    setModalHeader(header);
    setModalContent(modalContent);
    setIsModal(modal);
    setIsTransparent(transparent);
    setIsOpened(true);
  }
  const closeModal = (modalId?: ModalId) => {
    if (modalId && modalId !== lastModal.current) {
      return;
    }
    setIsOpened(false);
  };

  const headerWrapper = modalContent
    ? <div className='modal-header'>{modalHeader}</div>
    : modalHeader;

  const dialogElement = isOpened &&
    <Dialog
      header={headerWrapper}
      onHide={closeModal}
      visible={isOpened}
      headerStyle={{ padding: '0', borderRadius: '0' }}
      contentStyle={{ padding: '0' }}
      dismissableMask={isClosable}
      closable={false}
      resizable={false}
      draggable={false}
      modal={isModal}
      className={isTransparent ? "transparent-dialog" : ""}
      headerClassName={isTransparent ? "transparent-dialog" : ""}
    >
      {modalContent &&
        <div className='modal-content'>
          {modalContent}
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
