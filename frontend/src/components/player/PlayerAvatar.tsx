import { memo, MouseEventHandler } from "react";
import { getAvatarUrl } from "../../utils/playerAvatar";

interface IPlayerAvatarProps {
  avatarName: string,
  className?: string,
  onClickHandler?: MouseEventHandler<HTMLDivElement>,
}

const PlayerAvatar = ({ avatarName, onClickHandler, className }: IPlayerAvatarProps) => {
  return (
    <div
      className="player-avatar"
      onClick={onClickHandler}
    >
      <div
        className={className}
        style={{
          backgroundImage: `url(${getAvatarUrl(avatarName)})`,
        }}
      />
    </div>
  );
}

export default memo(PlayerAvatar);
