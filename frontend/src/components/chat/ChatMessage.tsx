import { memo } from 'react';
import { Message } from "primereact/message";

interface IProps {
  text: string,
  author?: string,
  ownMessage?: boolean,
  color?: string,
}

const ChatMessage = ({text, author, ownMessage, color}: IProps) => {
  const systemMessage = !author;
  const textContent = <span>{text}</span>
  if (ownMessage) {
    return <Message
      className='chat-message own-message'
      content={textContent}
      style={{background: color}}
    />
  } else if (systemMessage) {
    return <Message
      className='chat-message system-message'
      content={<><i className='pi pi-exclamation-circle icon'/> {textContent}</>}
    />
  } else {
    const content = (
      <>
        <span className="chat-message-author">{`${author}: `}</span>
        {textContent}
      </>
    );
    return (
      <Message
        className="chat-message player-message"
        content={content}
        style={{background: color}}
      />
    )
  }
}

export default memo(ChatMessage);