import { ChangeEvent, KeyboardEvent, memo, useRef, useState } from "react";

import { OverlayPanel } from "primereact/overlaypanel";
import { InputText } from "primereact/inputtext";

import StartPageBackground from "./StartPageBackground";
import StartPageButton from "./StartPageButton";
import { useAuthContext } from "../context/AuthContextProvider";
import { AVATAR_NAMES, getAvatarUrl, getRandomAvatar } from "../utils/playerAvatar";
import PlayerAvatar from "./player/PlayerAvatar";

const LoginForm = () => {

  const avatarOverlay = useRef<OverlayPanel>(null);
  const [avatar, setAvatar] = useState(getRandomAvatar());
  const { login, isLoginInProgress } = useAuthContext();
  const [nameInputValue, setNameInputValue] = useState('');
  const isInputInvalid = !nameInputValue || nameInputValue.length < 3 || nameInputValue.length > 20;

  const onInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    setNameInputValue(event.target.value);
  }

  const onJoinClickHandler = () => {
    !isInputInvalid && login({
      name: nameInputValue,
      avatar,
    });
  }

  const onEnterKeyDown = ({ key }: KeyboardEvent<any>) => {
    if (key === 'Enter') {
      onJoinClickHandler();
    }
  };

  return (
    <StartPageBackground>
      <PlayerAvatar
        avatarName={avatar}
        className="avatar-in-login"
        onClickHandler={(e) => avatarOverlay.current?.toggle(e)}
      />
      <OverlayPanel
        ref={avatarOverlay}
        dismissable
        closeOnEscape
      >
        {
          <div className="avatar-picker">
            {AVATAR_NAMES.map(
              (avatar) =>
                <PlayerAvatar
                  key={avatar}
                  className="avatar-in-picker"
                  avatarName={avatar}
                  onClickHandler={() => {
                    setAvatar(avatar);
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
