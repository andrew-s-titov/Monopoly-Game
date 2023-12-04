import { memo } from "react";

interface IGameRoomPlayerProps {
  name: string;
}

const GameRoomPlayer = ({ name }: IGameRoomPlayerProps) => {
  return (
    <div className="gr-player">
      <div className="gr-player-image"/>
      <div className="gr-player-name">{name}</div>
    </div>
  );
}

export default memo(GameRoomPlayer);
