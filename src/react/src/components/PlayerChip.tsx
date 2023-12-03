import { memo } from "react";

interface IPlayerChipProps {
  playerColor: string,
  top: string,
  left: string,
}

const PlayerChip = ({ playerColor, top, left }: IPlayerChipProps) => {
  return (
    <div
      className="player-chip"
      style={{
        top,
        left,
        backgroundColor: playerColor,
      }}
    >
    </div>
  );
}

export default memo(PlayerChip);
