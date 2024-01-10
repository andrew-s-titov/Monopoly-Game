import { memo } from "react";
import PlayerAvatar from "./player/PlayerAvatar";

interface IGameRoomPlayerProps {
  name: string,
  avatar: string,
  isOverview?: boolean,
}

const GameRoomPlayer = ({ name, avatar, isOverview = false }: IGameRoomPlayerProps) => {
  return (
    <div className={isOverview ? 'gr-overview-player' : 'gr-player'}>
      <PlayerAvatar
        avatarName={avatar}
        className={isOverview ? 'gr-overview-player-avatar' : 'gr-player-avatar'}
      />
      <div className={isOverview ? 'gr-overview-player-name' : 'gr-player-name'}>{name}</div>
    </div>
  );
}

export default memo(GameRoomPlayer);
