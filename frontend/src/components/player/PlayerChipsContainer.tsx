import { useMemo } from "react";

import { useGameState } from "../../context/GameStateProvider";
import PlayerChip from "./PlayerChip";

const getNumberProperty = (style: CSSStyleDeclaration, propertyName: string) => {
  return Number.parseInt(style.getPropertyValue(propertyName).trim());
}

interface PositioningProps {
  mountSize: number,
  step: number,
  startPosition: number,
  cornerStepAdjustment: number,
}

const PlayerChipsContainer = () => {

  const { gameState } = useGameState();

  const positioningProps: PositioningProps = useMemo(() => {
    const styleDeclaration = getComputedStyle(document.body);
    const fieldWideSide = getNumberProperty(styleDeclaration, '--wide');
    const fieldNarrowSide = getNumberProperty(styleDeclaration, '--narrow');
    const cornerStepAdjustment = (fieldWideSide - fieldNarrowSide) / 2;
    const chipWidthAdjustment = getNumberProperty(styleDeclaration, '--player-chip') / 2;
    return {
      mountSize: getNumberProperty(styleDeclaration, '--mount'),
      step: fieldNarrowSide + getNumberProperty(styleDeclaration, '--gap'),
      startPosition: fieldWideSide / 2 - chipWidthAdjustment,
      cornerStepAdjustment,
    }
  }, []);

  const calculateStylePropInVh = (value: number) => {
    return `${100 * value / positioningProps.mountSize}vh`;
  }

  const defineChipTop = (fieldIndex: number): string => {
    let adjustment = 0;
    if (fieldIndex >= 20 && fieldIndex <= 30) {
      adjustment = positioningProps.step * 10 + positioningProps.cornerStepAdjustment * 2;
    } else if (fieldIndex > 10 && fieldIndex < 20) {
      adjustment = positioningProps.cornerStepAdjustment + positioningProps.step * (fieldIndex - 10);
    } else if (fieldIndex > 30 && fieldIndex < 40) {
      adjustment = positioningProps.cornerStepAdjustment + positioningProps.step * (40 - fieldIndex);
    }
    return calculateStylePropInVh(positioningProps.startPosition + adjustment);
  }

  const defineChipLeft = (fieldIndex: number): string => {
    let adjustment = 0;
    if (fieldIndex >= 10 && fieldIndex <= 20) {
      adjustment = positioningProps.step * 10 + positioningProps.cornerStepAdjustment * 2;
    } else if (fieldIndex > 0 && fieldIndex < 10) {
      adjustment += positioningProps.step * fieldIndex + positioningProps.cornerStepAdjustment;
    } else if (fieldIndex > 20 && fieldIndex < 30) {
      adjustment += positioningProps.step * (30 - fieldIndex) + positioningProps.cornerStepAdjustment;
    }
    return calculateStylePropInVh(positioningProps.startPosition + adjustment);
  }

  const playerStates = gameState.playerStates;

  return (
    <div className="chips-container">
      {
        Object.entries(playerStates)
          .filter(([_, player]) => !player.bankrupt)
          .map(([playerId, player]) =>
            <PlayerChip
              key={`${playerId}-chip`}
              playerColor={player.color}
              top={defineChipTop(player.position)}
              left={defineChipLeft(player.position)}
            />
          )
      }
    </div>
  );
}

export default PlayerChipsContainer;
