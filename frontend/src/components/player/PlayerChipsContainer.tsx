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
  chipWidth: number,
}

const PlayerChipsContainer = () => {

  const { gameState } = useGameState();

  const positioningProps: PositioningProps = useMemo(() => {
    const styleDeclaration = getComputedStyle(document.body);
    const fieldWideSide = getNumberProperty(styleDeclaration, '--wide');
    const fieldNarrowSide = getNumberProperty(styleDeclaration, '--narrow');
    const cornerStepAdjustment = (fieldWideSide - fieldNarrowSide) / 2;
    const chipWidth = getNumberProperty(styleDeclaration, '--player-chip');
    const chipWidthAdjustment = chipWidth / 2;
    return {
      mountSize: getNumberProperty(styleDeclaration, '--mount'),
      step: fieldNarrowSide + getNumberProperty(styleDeclaration, '--gap'),
      startPosition: fieldWideSide / 2 - chipWidthAdjustment,
      cornerStepAdjustment,
      chipWidth,
    }
  }, []);

  const calculateStylePropInVh = (value: number) => {
    return `${100 * value / positioningProps.mountSize}vh`;
  }

  const defineChipTop = (fieldIndex: number, sameFieldMatchIndex: number): string => {
    let top = 0;
    if (fieldIndex >= 20 && fieldIndex <= 30) {
      top = positioningProps.step * 10 + positioningProps.cornerStepAdjustment * 2;
    } else if (fieldIndex > 10 && fieldIndex < 20) {
      top = positioningProps.cornerStepAdjustment + positioningProps.step * (fieldIndex - 10);
    } else if (fieldIndex > 30 && fieldIndex < 40) {
      top = positioningProps.cornerStepAdjustment + positioningProps.step * (40 - fieldIndex);
    }
    top = adjustSameFieldChipTop(positioningProps.startPosition + top, sameFieldMatchIndex);
    return calculateStylePropInVh(top);
  }

  const adjustSameFieldChipTop = (top: number, index: number) => {
    if (index === 1 || index === 4) {
      return top - positioningProps.chipWidth;
    } else if (index === 2 || index === 3) {
      return top + positioningProps.chipWidth;
    } else {
      return top;
    }
  }

  const defineChipLeft = (fieldIndex: number, sameFieldMatchIndex: number): string => {
    let left = 0;
    if (fieldIndex >= 10 && fieldIndex <= 20) {
      left = positioningProps.step * 10 + positioningProps.cornerStepAdjustment * 2;
    } else if (fieldIndex > 0 && fieldIndex < 10) {
      left += positioningProps.step * fieldIndex + positioningProps.cornerStepAdjustment;
    } else if (fieldIndex > 20 && fieldIndex < 30) {
      left += positioningProps.step * (30 - fieldIndex) + positioningProps.cornerStepAdjustment;
    }
    left = adjustSameFieldChipLeft(positioningProps.startPosition + left, sameFieldMatchIndex);
    return calculateStylePropInVh(left);
  }

  const adjustSameFieldChipLeft = (left: number, index: number) => {
    if (index === 1 || index === 2) {
      return left + positioningProps.chipWidth;
    } else if (index === 3 || index === 4) {
      return left - positioningProps.chipWidth;
    } else {
      return left;
    }
  }

  const playerStates = gameState.playerStates;

  const playersByPositions: Record<number, string[]> = Object.entries(playerStates)
        .filter(([_, player]) => !player.bankrupt)
        .reduce((resultProps, [id, { position }]) => {
          resultProps[position] = [
            ...resultProps[position] || [],
            id,
          ];
          return resultProps;
        }, {} as Record<number, string[]>);

  const playerIndexOnField = (id: string, position: number) => playersByPositions[position].indexOf(id) || 0;

  return (
    <div className="chips-container">
      {
        Object.entries(playerStates)
          .filter(([_, { bankrupt }]) => !bankrupt)
          .map(([playerId, { position, color }]) =>
            <PlayerChip
              key={`${playerId}-chip`}
              playerColor={color}
              top={defineChipTop(position, playerIndexOnField(playerId, position))}
              left={defineChipLeft(position, playerIndexOnField(playerId, position))}
            />
          )
      }
    </div>
  );
}

export default PlayerChipsContainer;
