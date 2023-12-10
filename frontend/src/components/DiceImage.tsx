import { memo } from "react";

interface IDiceImageProps {
  side: 'left' | 'right';
  result?: number;
}

const DiceImage = ({ side, result }: IDiceImageProps) => {

  const src = require(`../assets/images/dice/dice${result ? `${result}.png` : `-${side}.gif`}`);
  const alt = `${side} dice`;

  return (
    <img
      width="18vh"
      className="dice"
      src={src}
      alt={alt}
    />
  );
}

export default memo(DiceImage);
