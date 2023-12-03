import { createContext, PropsWithChildren, useContext, useRef } from 'react';
import { Toast } from 'primereact/toast';

interface IMessageContext {
  showWarning: (text: string) => void;
  showError: (text: string) => void;
}

const MessageContext = createContext({} as IMessageContext);

export const MessageProvider = ({ children }: PropsWithChildren) => {

  const messageToast = useRef<Toast>(null);

  const showToast = (detail: string, severity: 'success' | 'info' | 'warn' | 'error', life: number) => {
    messageToast.current?.show({
      severity,
      detail,
      life
    });
  }

  const showWarning = (text: string) => showToast(text, 'warn', 3000);
  const showError = (text: string) => showToast(text, 'error', 5000);

  return (
    <MessageContext.Provider value={{ showWarning, showError }}>
      <Toast ref={messageToast}/>
      {children}
    </MessageContext.Provider>
  );
}

export const useMessageContext = () => useContext(MessageContext);