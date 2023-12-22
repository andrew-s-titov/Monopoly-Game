import { memo, MouseEventHandler } from "react";
import { getAvatarUrl } from "../../utils/playerAvatar";

interface IPlayerAvatarProps {
  avatarName: string,
  className?: string,
  onClickHandler?: MouseEventHandler<HTMLDivElement>,
  withPointer?: boolean,
}

const PlayerAvatar = ({ avatarName, onClickHandler, className, withPointer }: IPlayerAvatarProps) => {
  return (
    <div
      onClick={onClickHandler}
      className={`player-avatar ${className}`}
      style={{
        backgroundImage: `url(${getAvatarUrl(avatarName)})`,
        cursor: withPointer ? 'pointer' : '',
      }}
    >
        <div
            className={className}
            style={{
                backgroundImage: `url(${require('../../assets/images/avatars/ny-cap.png')})`
            }}
        />
    </div>
  );
}

export default memo(PlayerAvatar);
