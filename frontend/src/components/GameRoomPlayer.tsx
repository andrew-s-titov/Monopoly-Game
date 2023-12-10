import { memo } from "react";
import PlayerAvatar from "./player/PlayerAvatar";

interface IGameRoomPlayerProps {
  name: string,
  avatar: string,
}

const GameRoomPlayer = ({ name, avatar }: IGameRoomPlayerProps) => {
  return (
    <div className="gr-player">
      <PlayerAvatar
        avatarName={avatar}
        className="avatar-in-room"
      />
      <div className="gr-player-name">{name}</div>
    </div>
  );
}

export default memo(GameRoomPlayer);
