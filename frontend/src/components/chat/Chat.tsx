import { memo } from "react";
import ChatHistory from "./ChatHistory";
import ChatInput from "./ChatInput";
import { useGameState } from "../../context/GameStateProvider";
import { useWebsocketContext } from "../../context/WebsocketConnectionProvider";

const Chat = () => {
  const { messages } = useGameState();
  const { sendMessage } = useWebsocketContext();

  return (
    <>
      <ChatHistory messages={ messages }/>
      <ChatInput sendMessage={ sendMessage }/>
    </>
  );
}

export default memo(Chat);
