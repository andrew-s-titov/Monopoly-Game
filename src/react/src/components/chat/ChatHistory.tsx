import { memo, useEffect, useRef, useState } from 'react';

import { ChatMessageBody } from '../../types/interfaces';
import ChatMessage from './ChatMessage';
import { Button } from 'primereact/button';
import { useGameState } from "../../context/GameStateProvider";
import { getLoggedInUserId } from "../../utils/auth";

interface IChatContainerProps {
  messages: ChatMessageBody[],
}

const ChatHistory = ({ messages }: IChatContainerProps) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [showScrollButton, setShowScrollButton] = useState(false);
  const { gameState } = useGameState();
  const loggedInUserId = getLoggedInUserId();

  useEffect(() => {
    const container = containerRef.current;
    const messageAmount = container && container.children.length;
    if (container && messageAmount && messageAmount > 1) {
      const containerWholeHeight = container.scrollHeight;
      const preLatestChildHeight = container.children[messageAmount - 2].clientHeight;
      const containerVisibleHeight = container.clientHeight;
      const scrollCurrentTop = container.scrollTop;
      // if message before the new one is at least partially visible - scroll to bottom
      if ((scrollCurrentTop + containerVisibleHeight + preLatestChildHeight)
        > (containerWholeHeight - preLatestChildHeight)) {
        container.scrollTop = container.scrollHeight;
      }
    }
  }, [messages]);

  const handleScroll = () => {
    containerRef.current && setShowScrollButton(
      containerRef.current.scrollTop <
      (containerRef.current.scrollHeight - containerRef.current.clientHeight) * 0.8);
  }

  const scrollDown = () => {
    if (containerRef.current) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight;
    }
  }

  return (
    <div
      ref={containerRef}
      className='chat'
      onScroll={handleScroll}
    >
      <div className='chat-filler'></div>
      {messages.map((message, index) => {
        return (
          <ChatMessage
            key={index}
            text={message.message}
            author={message.playerId ? gameState.playerStates[message.playerId]?.name : undefined}
            color={message.playerId ? gameState.playerStates[message.playerId]?.color : undefined}
            ownMessage={loggedInUserId === message.playerId}
          />)
      })}
      {showScrollButton &&
        <Button
          className='scroll-down'
          severity='secondary'
          icon='pi pi-angle-down icon'
          onClick={scrollDown}
          rounded
          text
          raised
        />
      }
    </div>);
}

export default memo(ChatHistory);