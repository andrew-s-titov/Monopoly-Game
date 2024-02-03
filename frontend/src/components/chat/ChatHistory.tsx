import { ReactNode, useEffect, useRef, useState } from 'react';
import { Button } from 'primereact/button';

interface IChatContainerProps {
  messages: ReactNode[],
}

const ChatHistory = ({ messages }: IChatContainerProps) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [showScrollButton, setShowScrollButton] = useState(false);

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
    defineShowScrollButtonVisibility(container);
  }, [messages]);

  const defineShowScrollButtonVisibility = (div: HTMLDivElement | null) => {
    div && setShowScrollButton(
      div.scrollTop < (div.scrollHeight - div.clientHeight) * 0.8);
  }

  const handleScroll = () => {
    defineShowScrollButtonVisibility(containerRef.current);
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
      {messages.map((message, index) => <div key={index}>{message}</div>)}
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

export default ChatHistory;
