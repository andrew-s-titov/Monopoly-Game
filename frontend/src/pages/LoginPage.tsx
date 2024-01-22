import { ChangeEvent, KeyboardEvent, memo, useRef, useState } from "react";

import { OverlayPanel } from "primereact/overlaypanel";
import { InputText } from "primereact/inputtext";

import StartPageBackground from "../components/StartPageBackground";
import StartPageButton from "../components/StartPageButton";
import { useAuthContext } from "../context/AuthContextProvider";
import { AVATAR_NAMES, getRandomAvatar } from "../utils/playerAvatar";
import PlayerAvatar from "../components/player/PlayerAvatar";
import StartPageCenteredContent from "../components/StartPageCenteredContent";
import useQuery from "../hooks/useQuery";
import { BE_ENDPOINT } from "../config/api";
import { LoginResponse } from "../types/interfaces";
import { setAuthData } from "../utils/auth";

const LoginPage = () => {

  const avatarOverlay = useRef<OverlayPanel>(null);
  const [avatar, setAvatar] = useState(getRandomAvatar());
  const { setLoggedIn } = useAuthContext();
  const [nameInputValue, setNameInputValue] = useState('');

  const { post } = useQuery();
  const { execute: login, isLoading: isLoginInProgress } = post({
    url: `${BE_ENDPOINT}`,
    responseHandler: (loginResponse: LoginResponse) => {
      setAuthData(loginResponse);
      setLoggedIn();
    }
  });

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
    <StartPageBackground withHeader={false}>
      <StartPageCenteredContent>
        <PlayerAvatar
          avatarName={avatar}
          className="avatar-in-login"
          onClickHandler={(e) => avatarOverlay.current?.toggle(e)}
          withPointer
        />
        <InputText
          autoFocus
          value={nameInputValue}
          onChange={onInputChange}
          className="player-name-input"
          placeholder='Enter your nickname'
          onKeyDown={onEnterKeyDown}
        />
        <StartPageButton
          label='Create user'
          icon="pi-user"
          isLoading={isLoginInProgress}
          isDisabled={isInputInvalid}
          onClickHandler={onJoinClickHandler}
        />
      </StartPageCenteredContent>
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
                  withPointer
                />
            )}
          </div>
        }
      </OverlayPanel>
    </StartPageBackground>
  );
}

export default memo(LoginPage);
