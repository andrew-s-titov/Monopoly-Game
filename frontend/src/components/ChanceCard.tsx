import { memo } from "react";

interface IChanceCardProps {
  text: string,
}

const ChanceCard = ({ text }: IChanceCardProps) => {

  return (
    <div
      className="chance-card"
    >
      {text}
    </div>
  );
}

export default memo(ChanceCard);
