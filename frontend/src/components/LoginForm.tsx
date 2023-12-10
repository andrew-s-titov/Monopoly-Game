import { ChangeEvent, KeyboardEvent, memo, useRef, useState } from "react";

import { OverlayPanel } from "primereact/overlaypanel";
import { InputText } from "primereact/inputtext";

import StartPageBackground from "./StartPageBackground";
import StartPageButton from "./StartPageButton";
import { useAuthContext } from "../context/AuthContextProvider";
import { AVATARS, getRandomAvatar } from "../utils/playerAvatar";
import PlayerAvatar from "./player/PlayerAvatar";

const LoginForm = () => {

  const avatarOverlay = useRef<OverlayPanel>(null);
  const [avatar, setAvatar] = useState(getRandomAvatar());
  const { loginWithName, isLoginInProgress } = useAuthContext();
  const [nameInputValue, setNameInputValue] = useState('');
  const isInputInvalid = !nameInputValue || nameInputValue.length < 3 || nameInputValue.length > 20;

  const onInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    setNameInputValue(event.target.value);
  }

  const onJoinClickHandler = () => {
    !isInputInvalid && loginWithName(nameInputValue);
  }

  const onEnterKeyDown = ({ key }: KeyboardEvent<any>) => {
    if (key === 'Enter') {
      onJoinClickHandler();
    }
  };

  return (
    <StartPageBackground>
      <PlayerAvatar
        url={avatar}
        onClickHandler={(e) => avatarOverlay.current?.toggle(e)}
      />
      <OverlayPanel
        ref={avatarOverlay}
        dismissable
        closeOnEscape
      >
        {
          <div className="avatar-picker">
            {Object.entries(AVATARS).map(
              ([name, url]) =>
                <PlayerAvatar
                  key={name}
                  url={url}
                  isSmall={true}
                  onClickHandler={() => {
                    setAvatar(url);
                  }}
                />
            )}
          </div>
        }
      </OverlayPanel>
      <InputText
        autoFocus
        value={nameInputValue}
        onChange={onInputChange}
        className="player-name-input"
        placeholder='Enter your nickname'
        onKeyDown={onEnterKeyDown}
      />
      <StartPageButton
        label='Join the game'
        icon="pi-user"
        isLoading={isLoginInProgress}
        isDisabled={isInputInvalid}
        onClickHandler={onJoinClickHandler}
      />
    </StartPageBackground>
  );
}

export default memo(LoginForm);
