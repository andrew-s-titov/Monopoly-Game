import { KeyboardEvent, memo, useRef } from 'react';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';

interface IChatInputProps {
  sendMessage: (message: string) => void;
}

const ChatInput = ({ sendMessage }: IChatInputProps) => {

  const inputRef = useRef<HTMLInputElement>(null);

  const onSendClick = () => {
    if (inputRef.current && inputRef.current.value) {
      sendMessage(inputRef.current.value);
      inputRef.current.value = '';
    }
  }

  const onEnterKeyDown = ({ key }: KeyboardEvent<any>) => {
    if (key === 'Enter') {
      onSendClick();
    }
  };

  return (
    <div className='p-inputgroup chat-input-container' onKeyDown={onEnterKeyDown}>
      <InputText
        className='chat-input'
        ref={inputRef}
        placeholder='Enter your message here...'
      />
      <Button
        className='send-message'
        icon='pi pi-send icon'
        severity='secondary'
        onClick={onSendClick}
        onKeyDown={onEnterKeyDown}
      />
    </div>
  );
}

export default memo(ChatInput);