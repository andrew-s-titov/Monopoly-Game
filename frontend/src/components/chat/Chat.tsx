import { memo } from "react";
import ChatHistory from "./ChatHistory";
import ChatInput from "./ChatInput";
import { useGameState } from "../../context/GameStateProvider";
import { useActiveGameContext } from "../../context/ActiveGameContextProvider";

const Chat = () => {
  const { messages } = useGameState();
  const { sendMessage } = useActiveGameContext();

  return (
    <>
      <ChatHistory messages={messages}/>
      <ChatInput sendMessage={sendMessage}/>
    </>
  );
}

export default memo(Chat);
