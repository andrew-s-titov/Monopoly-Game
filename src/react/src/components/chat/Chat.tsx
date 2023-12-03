import { memo, useMemo } from "react";
import ChatHistory from "./ChatHistory";
import ChatInput from "./ChatInput";
import { useGameState } from "../../context/GameStateProvider";
import { getLoggedInUserId } from "../../utils/auth";
import { useWebsocketContext } from "../../context/WebsocketConnectionProvider";

const Chat = () => {
  const { messages } = useGameState();
  const { sendMessage } = useWebsocketContext();

  const loggedInUserId = useMemo(getLoggedInUserId, []);

  // TODO: on send -> call backend
  // TODO: on websocket message -> add message
  // TODO: get message author from websocket message
  const addMessageToHistory = (text: string) => sendMessage(
    {
      message: text,
      playerId: loggedInUserId,
    });

  return (
    <>
      <ChatHistory messages={ messages }/>
      <ChatInput sendMessage={ addMessageToHistory }/>
    </>
  );
}

export default memo(Chat);
