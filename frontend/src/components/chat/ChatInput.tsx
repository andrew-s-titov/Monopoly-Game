import { KeyboardEvent, memo, useRef, useState } from 'react';
import { InputText } from 'primereact/inputtext';
import EmojiPicker, { EmojiStyle, SuggestionMode } from "emoji-picker-react";
import { OverlayPanel } from "primereact/overlaypanel";

interface IChatInputProps {
  sendMessage: (message: string) => void;
}

const ChatInput = ({ sendMessage }: IChatInputProps) => {

  const emojiPickerRef = useRef<OverlayPanel>(null);
  const [message, setMessage] = useState('');

  const onSendClick = () => {
    if (message) {
      emojiPickerRef.current?.hide();
      sendMessage(message);
      setMessage('');
    }
  }

  const onEnterKeyDown = ({ key }: KeyboardEvent<any>) => {
    if (key === 'Enter') {
      onSendClick();
    }
  };

  return (
    <div className='chat-input-container' onKeyDown={onEnterKeyDown}>
      <InputText
        unstyled
        className='chat-input'
        value={message}
        onChange={e => setMessage(e.target.value)}
        placeholder='Enter your message here...'
      />

      <div
        onClick={(e) => emojiPickerRef.current?.toggle(e)}
        className="emoji-picker-button"
      />
      <OverlayPanel
        ref={emojiPickerRef}
        dismissable
        closeOnEscape
      >
        <EmojiPicker
          suggestedEmojisMode={SuggestionMode.RECENT}
          emojiVersion={"5.0"}
          emojiStyle={EmojiStyle.NATIVE}
          lazyLoadEmojis={true}
          previewConfig={{
            showPreview: false,
          }}
          onEmojiClick={e => setMessage(m => m + e.emoji)}
        />
      </OverlayPanel>
      <button
        className='send-message'
        onClick={onSendClick}
      >
        <p className="pi pi-send icon"/>
      </button>
    </div>
  );
}

export default memo(ChatInput);