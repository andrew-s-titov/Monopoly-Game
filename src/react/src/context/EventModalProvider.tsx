import { createContext, PropsWithChildren, useContext } from "react";

import useModal, { IModalProps } from "../hooks/useModal";

export interface IEventModalContext {
  openEventModal: (modalProps: IModalProps) => void;
  closeEventModal: () => void;
}

const EventModalContext = createContext({} as IEventModalContext);

export const EventModalProvider = ({children}: PropsWithChildren) => {

  const {openModal, closeModal, dialogElement} = useModal();

  return (
    <EventModalContext.Provider value={{openEventModal: openModal, closeEventModal: closeModal}}>
      {children}
      {dialogElement}
    </EventModalContext.Provider>
  );
}

export const useEventModalContext = () => useContext(EventModalContext);
