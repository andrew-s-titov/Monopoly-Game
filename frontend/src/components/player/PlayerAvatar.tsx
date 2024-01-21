import { memo, MouseEventHandler } from "react";
import { getAvatarUrl } from "../../utils/playerAvatar";

interface IPlayerAvatarProps {
  avatarName: string,
  className: string,
  onClickHandler?: MouseEventHandler<HTMLDivElement>,
  withPointer?: boolean,
}

const PlayerAvatar = ({ avatarName, className, onClickHandler, withPointer }: IPlayerAvatarProps) => {
  return (
    <div
      onClick={onClickHandler}
      className={`player-avatar ${className}`}
      style={{
        backgroundImage: `url(${getAvatarUrl(avatarName)})`,
        cursor: withPointer ? 'pointer' : '',
      }}
    />
  );
}

export default memo(PlayerAvatar);
