import { memo } from "react";
import { Button } from "primereact/button";

interface IStartPageButtonProps {
  icon: string;
  isLoading: boolean;
  isDisabled: boolean;
  onClickHandler: () => void;
}

const StartPageButton = ({ icon, isLoading, isDisabled, onClickHandler }: IStartPageButtonProps) => {

  return (
    <Button
      icon={`pi ${icon} icon`}
      loading={isLoading}
      disabled={isDisabled}
      onClick={onClickHandler}
      loadingIcon="pi pi-spin pi-spinner icon"
      className="non-game-button"
      label='Join the game'
      text
      raised
    />
  );
}

export default memo(StartPageButton);
