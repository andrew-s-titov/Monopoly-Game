import { useEffect, useRef } from "react";
import { useMessageContext } from "../context/MessageProvider";
import { useRouting } from "../context/Routing";

interface IWebsocketProps {
  url: string,
  onMessage: (data: MessageEvent) => void,
  onDestroy?: () => void,
  retries?: number,
}

const defaultRetries = 5;

const useWebsocket = ({ url, onMessage, onDestroy, retries }: IWebsocketProps) => {

  const websocket = useRef<WebSocket>();
  const connectionRetries = useRef(0);
  const delayedRetry = useRef<ReturnType<typeof setTimeout>>();
  const { showWarning } = useMessageContext();
  const { navigate } = useRouting();

  const maxRetries = retries ? retries : defaultRetries;
  const sendToServer: WebSocket['send'] = websocket.current ? websocket.current.send : () => {};

  const connectWebSocket = () => {
    websocket.current = new WebSocket(url);

    websocket.current.onopen = () => {
      connectionRetries.current = 0;
    };

    websocket.current.onmessage = onMessage;

    websocket.current.onclose = ({ code, reason }: CloseEvent) => {
      console.log(`WebSocket close, code=${code}, reason=(${reason})`);
      if (code === 1000 || code === 1001) {
        // do nothing - normal close
      } else if (code === 1006 && connectionRetries.current < maxRetries) {
        console.log(`Unexpected WebSocket close, reconnecting in 1 sec...`);
        connectionRetries.current = connectionRetries.current + 1;
        delayedRetry.current = setTimeout(connectWebSocket, 1000);
      } else {
        // any other close code or max retries are reached:
        showWarning('Server connection error. Please reload the page or try again later');
        navigate('home');
      }
    }
  }

  useEffect(() => {
    connectWebSocket();

    return () => {
      delayedRetry.current && clearTimeout(delayedRetry.current);
      onDestroy && onDestroy();
      websocket.current
      && websocket.current?.readyState === websocket.current?.OPEN
      && websocket.current.close(1000, 'page with WebSocket connection was closed');
    }
  }, []);

  return {
    sendToServer,
  };
}

export default useWebsocket;
