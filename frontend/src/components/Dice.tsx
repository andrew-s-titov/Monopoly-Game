import { memo } from "react";
import DiceImage from "./DiceImage";

interface IDiceProps {
  result?: [number, number];
}

const Dice = ({ result }: IDiceProps) => {
  return (
    <div className="dice-container">
      {result
        ? <>
          <DiceImage side="left" result={result[0]}/>
          <DiceImage side="right" result={result[1]}/>
        </>
        : <>
          <DiceImage side="left"/>
          <DiceImage side="right"/>
        </>
      }
    </div>
  );
};

export default memo(Dice);
