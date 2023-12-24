import { createContext, PropsWithChildren, ReactNode, useContext, useRef } from 'react';
import { Toast } from 'primereact/toast';

interface IMessageContext {
  showWarning: (text: string) => void,
  showError: (text: string) => void,
  showCenterPopUp: (content: ReactNode) => void,
}

const MessageContext = createContext({} as IMessageContext);

export const MessageProvider = ({ children }: PropsWithChildren) => {

  const popUpToast = useRef<Toast>(null);
  const centerToast = useRef<Toast>(null);

  const showToast = (detail: string, severity: 'success' | 'info' | 'warn' | 'error', life: number) => {
    popUpToast.current?.show({
      severity,
      detail,
      life
    });
  }

  const showWarning = (text: string) => showToast(text, 'warn', 3000);
  const showError = (text: string) => showToast(text, 'error', 5000);

  const showCenterPopUp = (content: ReactNode) => centerToast.current?.show({
    className: "center-popup-content",
    life: 3000,
    content,
  });

  return (
    <MessageContext.Provider value={{ showWarning, showError, showCenterPopUp }}>
      <Toast ref={popUpToast}/>
      <Toast
        className="center-popup"
        ref={centerToast}
        position="bottom-center"
      />
      {children}
    </MessageContext.Provider>
  );
}

export const useMessageContext = () => useContext(MessageContext);
