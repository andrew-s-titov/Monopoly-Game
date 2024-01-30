import { KeyboardEvent, memo, useRef, useState } from 'react';
import { InputText } from 'primereact/inputtext';
import EmojiPicker, { Categories, EmojiStyle, SuggestionMode } from "emoji-picker-react";
import { OverlayPanel } from "primereact/overlaypanel";
import { useTranslations } from "../../i18n/config";
import { Button } from "primereact/button";

interface IChatInputProps {
  sendMessage: (message: string) => void;
}

const ChatInput = ({ sendMessage }: IChatInputProps) => {

  const { t } = useTranslations();
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
    <div className='chat-input-container'>
      <InputText
        unstyled
        className='chat-input'
        value={message}
        onChange={e => setMessage(e.target.value)}
        placeholder={t('placeholder.message')}
        onKeyDown={onEnterKeyDown}
      />
      <Button
        text
        onClick={(e) => emojiPickerRef.current?.toggle(e)}
        className="emoji-button"
      >
        <svg className="emoji" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
          <path
            d="M12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm5.507 13.941c-1.512 1.195-3.174 1.931-5.506 1.931-2.334 0-3.996-.736-5.508-1.931l-.493.493c1.127 1.72 3.2 3.566 6.001 3.566 2.8 0 4.872-1.846 5.999-3.566l-.493-.493zm-9.007-5.941c-.828 0-1.5.671-1.5 1.5s.672 1.5 1.5 1.5 1.5-.671 1.5-1.5-.672-1.5-1.5-1.5zm7 0c-.828 0-1.5.671-1.5 1.5s.672 1.5 1.5 1.5 1.5-.671 1.5-1.5-.672-1.5-1.5-1.5z"/>
        </svg>
      </Button>
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
          searchPlaceholder={t('emoji.search')}
          categories={[
            { category: Categories.SUGGESTED, name: t('emoji.recent') },
            { category: Categories.SMILEYS_PEOPLE, name: t('emoji.people') },
            { category: Categories.ANIMALS_NATURE, name: t('emoji.nature') },
            { category: Categories.FOOD_DRINK, name: t('emoji.food') },
            { category: Categories.TRAVEL_PLACES, name: t('emoji.travel') },
            { category: Categories.ACTIVITIES, name: t('emoji.activities') },
            { category: Categories.OBJECTS, name: t('emoji.objects') },
            { category: Categories.SYMBOLS, name: t('emoji.symbols') },
          ]}
        />
      </OverlayPanel>
      <Button
        text
        className='send-message'
        onClick={onSendClick}
      >
        <p className="pi pi-send icon"/>
      </Button>
    </div>
  );
}

export default memo(ChatInput);