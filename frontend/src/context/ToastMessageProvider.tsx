import { createContext, PropsWithChildren, ReactNode, useContext, useRef } from 'react';
import { Toast, ToastMessage } from 'primereact/toast';

interface IToastContext {
  showWarning: (text: string) => void,
  showError: (text: string) => void,
  showCenterPopUp: (content: ReactNode) => void,
}

type Severity = Exclude<ToastMessage['severity'], undefined>;

const ToastMessageContext = createContext({} as IToastContext);

export const ToastMessageProvider = ({ children }: PropsWithChildren) => {

  const popUpToast = useRef<Toast>(null);
  const centerToast = useRef<Toast>(null);

  const showToast = (detail: string, severity: Severity, life: number) => {
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
    <ToastMessageContext.Provider value={{ showWarning, showError, showCenterPopUp }}>
      <Toast ref={popUpToast}/>
      <Toast
        className="center-popup"
        ref={centerToast}
        position="bottom-center"
        onClick={message => centerToast.current?.remove(message)}
      />
      {children}
    </ToastMessageContext.Provider>
  );
}

export const useToastMessageContext = () => useContext(ToastMessageContext);
