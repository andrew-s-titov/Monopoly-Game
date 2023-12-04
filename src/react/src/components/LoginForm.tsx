import { ChangeEvent, KeyboardEvent, memo, useState } from "react";

import { InputText } from "primereact/inputtext";

import StartPageBackground from "./StartPageBackground";
import StartPageButton from "./StartPageButton";
import { useAuthContext } from "../context/AuthContextProvider";

const LoginForm = () => {

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
      <InputText
        autoFocus
        value={nameInputValue}
        onChange={onInputChange}
        className={isInputInvalid ? 'player-name-input player-name-input-invalid' : 'player-name-input'}
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
