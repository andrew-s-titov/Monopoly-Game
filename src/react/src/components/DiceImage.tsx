import { memo } from "react";

interface IDiceImageProps {
  side: 'left' | 'right';
  result?: number;
}

const DiceImage = ({ side, result }: IDiceImageProps) => {

  const src = result
    ? require(`../assets/images/dice${result}.png`)
    : require(`../assets/images/dice-${side}.gif`);
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
