import { memo, MouseEventHandler } from "react";

interface IPlayerAvatarProps {
  url: string,
  isSmall?: boolean,
  onClickHandler?: MouseEventHandler<HTMLDivElement>,
}

const PlayerAvatar = ({ url, isSmall, onClickHandler }: IPlayerAvatarProps) => {
  return (
    <div
      className={isSmall? "login-avatar-small" : "login-avatar"}
      onClick={onClickHandler}
    >
      <div
        style={{
          backgroundImage: `url(${url})`,
        }}
      />
    </div>
  );
}

export default memo(PlayerAvatar);
