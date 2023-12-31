import React, { useState } from "react";
import { Dialog } from "primereact/dialog";

export interface IModalProps {
  header: React.JSX.Element;
  modalContent?: React.JSX.Element;
  modal?: boolean;
  transparent?: boolean,
}

const useModal = (isClosable?: boolean) => {
  const [modalContent, setModalContent] = useState<React.JSX.Element>();
  const [modalHeader, setModalHeader] = useState<React.JSX.Element>();
  const [isOpened, setIsOpened] = useState(false);
  const [isModal, setIsModal] = useState(true);
  const [isTransparent, setIsTransparent] = useState(false);

  const openModal = ({
                       header,
                       modalContent,
                       modal = true,
                       transparent = false
                     }: IModalProps) => {
    setModalHeader(header);
    setModalContent(modalContent);
    setIsModal(modal);
    setIsTransparent(transparent);
    setIsOpened(true);
  }
  const closeModal = () => {
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
