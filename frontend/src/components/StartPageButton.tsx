import { memo } from "react";
import { Button } from "primereact/button";

interface IStartPageButtonProps {
  label: string,
  icon: string;
  isLoading: boolean;
  isDisabled: boolean;
  onClickHandler: () => void;
}

const StartPageButton = ({ label, icon, isLoading, isDisabled, onClickHandler }: IStartPageButtonProps) => {

  return (
    <Button
      icon={`pi ${icon} icon`}
      loading={isLoading}
      disabled={isDisabled}
      onClick={onClickHandler}
      loadingIcon="pi pi-spin pi-box icon"
      className="non-game-button"
      label={label}
      text
      raised
    />
  );
}

export default memo(StartPageButton);
