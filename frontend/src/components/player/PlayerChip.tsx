export interface IPlayerChipProps {
  color: string,
  top: string,
  left: string,
  transitionMs: number,
}

const PlayerChip = ({ color, top, left, transitionMs }: IPlayerChipProps) => {
  return (
    <div
      className="player-chip"
      style={{
        top,
        left,
        backgroundColor: color,
        transition: `
          left ${transitionMs}ms linear, 
          top ${transitionMs}ms linear,
          right ${transitionMs}ms linear,
          down ${transitionMs}ms linear`,
      }}
    >
    </div>
  );
}

export default PlayerChip;
