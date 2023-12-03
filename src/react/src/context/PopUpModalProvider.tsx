import { createContext, PropsWithChildren, useContext } from "react";

import useModal, { IModalProps } from "../hooks/useModal";

export interface IPopUpModalContext {
  openPopUpModal: (modalProps: IModalProps) => void;
  closePopUpModal: () => void;
}

const PopUpModalContext = createContext({} as IPopUpModalContext);

export const PopUpModalProvider = ({children}: PropsWithChildren) => {

  const {openModal, closeModal, dialogElement} = useModal(true);

  return (
    <PopUpModalContext.Provider value={{openPopUpModal: openModal, closePopUpModal: closeModal}}>
      {children}
      {dialogElement}
    </PopUpModalContext.Provider>
  );
}

export const usePopUpModalContext = () => useContext(PopUpModalContext);
