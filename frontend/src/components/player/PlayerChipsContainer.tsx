import { useMemo, useState } from "react";

import { useGameState } from "../../context/GameStateProvider";
import PlayerChip, { IPlayerChipProps } from "./PlayerChip";

interface PositioningProps {
  mountSize: number,
  step: number,
  startPosition: number,
  cornerStepAdjustment: number,
  chipWidth: number,
}

interface ChipPosition {
  top: string,
  left: string,
}

interface ChipParams extends IPlayerChipProps {
  endPosition: number,
}

interface MoveStep {
  index: number,
  transitionMs: number,
}

const moveTotalMs = 500;

const getNumberProperty = (style: CSSStyleDeclaration, propertyName: string) => {
  return Number.parseInt(style.getPropertyValue(propertyName).trim());
}

const buildPositioningProps = (): PositioningProps => {
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
};

const calculatePathForward = (oldPosition: number, newPosition: number): MoveStep[] => {
  const corners = [0, 10, 20, 30];

  let closestForwardCornerIndex = Math.floor(oldPosition / 10) + 1;
  let numberOfSteps = newPosition >= oldPosition ? newPosition - oldPosition : newPosition - oldPosition + 40;
  const stepTime = moveTotalMs / numberOfSteps;
  const path: MoveStep[] = [];

  let temporaryPosition = oldPosition;
  while (numberOfSteps > 0) {
    if (closestForwardCornerIndex === corners.length) {
      closestForwardCornerIndex = 0;
    }
    const nextCornerValue = closestForwardCornerIndex === 0 ? 40 : corners[closestForwardCornerIndex];
    const stepTillNextCorner = nextCornerValue - temporaryPosition;
    const stepsOfNextMove = Math.min(numberOfSteps, stepTillNextCorner);
    temporaryPosition += stepsOfNextMove;
    if (temporaryPosition > 39) {
      temporaryPosition = temporaryPosition - 40;
    }
    path.push({
      index: temporaryPosition,
      transitionMs: stepTime * stepsOfNextMove,
    })
    numberOfSteps -= stepsOfNextMove;
    closestForwardCornerIndex++;
  }
  return path;
}

const stylePropInVh = (value: number, positioningProps: PositioningProps) =>
  `${100 * value / positioningProps.mountSize}vh`;

const calculateChipPosition = (fieldIndex: number, positioningProps: PositioningProps,
                               playersOnPosition: Record<number, number>,
                               sameFieldIndex?: number): ChipPosition => {
  let top = positioningProps.startPosition;
  if (fieldIndex >= 20 && fieldIndex <= 30) {
    top += positioningProps.step * 10 + positioningProps.cornerStepAdjustment * 2;
  } else if (fieldIndex > 10 && fieldIndex < 20) {
    top += positioningProps.cornerStepAdjustment + positioningProps.step * (fieldIndex - 10);
  } else if (fieldIndex > 30 && fieldIndex < 40) {
    top += positioningProps.cornerStepAdjustment + positioningProps.step * (40 - fieldIndex);
  }

  let left = positioningProps.startPosition;
  if (fieldIndex >= 10 && fieldIndex <= 20) {
    left += positioningProps.step * 10 + positioningProps.cornerStepAdjustment * 2;
  } else if (fieldIndex > 0 && fieldIndex < 10) {
    left += positioningProps.step * fieldIndex + positioningProps.cornerStepAdjustment;
  } else if (fieldIndex > 20 && fieldIndex < 30) {
    left += positioningProps.step * (30 - fieldIndex) + positioningProps.cornerStepAdjustment;
  }

  if (sameFieldIndex && playersOnPosition[fieldIndex] && playersOnPosition[fieldIndex] > 1) {
    top = adjustSameFieldChipTop(top, sameFieldIndex, positioningProps);
    left = adjustSameFieldChipLeft(left, sameFieldIndex, positioningProps);
  }

  return {
    top: stylePropInVh(top, positioningProps),
    left: stylePropInVh(left, positioningProps),
  };
}

const adjustSameFieldChipTop = (baseTop: number, chipArrayIndex: number, positioningProps: PositioningProps) => {
  if (chipArrayIndex === 1 || chipArrayIndex === 4) {
    return baseTop - positioningProps.chipWidth;
  } else if (chipArrayIndex === 2 || chipArrayIndex === 3) {
    return baseTop + positioningProps.chipWidth;
  } else {
    return baseTop;
  }
}

const adjustSameFieldChipLeft = (left: number, index: number, positioningProps: PositioningProps) => {
  if (index === 1 || index === 2) {
    return left + positioningProps.chipWidth;
  } else if (index === 3 || index === 4) {
    return left - positioningProps.chipWidth;
  } else {
    return left;
  }
}

const PlayerChipsContainer = () => {

  const { gameState, chipMove } = useGameState();
  const playerStates = gameState.playerStates;

  const playersOnPosition: Record<number, number> = Object.entries(playerStates)
    .filter(([_, player]) => !player.bankrupt)
    .map(([_, player]) => player.position)
    .reduce((resultMap, position) => {
      if (resultMap[position]) {
        resultMap[position]++;
      } else {
        resultMap[position] = 1;
      }
      return resultMap;
    }, {} as Record<number, number>);

  const positioningProps = useMemo(buildPositioningProps, []);
  const defineChipPosition = (fieldIndex: number, sameFieldIndex?: number) => calculateChipPosition(
    fieldIndex, positioningProps, playersOnPosition, sameFieldIndex);

  const [playerChipParams, setPlayerChipParams] = useState<Record<string, ChipParams>>(
    () => Object.entries(playerStates)
      .filter(([_, { bankrupt }]) => !bankrupt)
      .reduce((params, [playerId, { color, position }], arrayIndex) => {
        const chipPosition = defineChipPosition(position, arrayIndex);
        params[playerId] = {
          endPosition: position,
          color,
          top: chipPosition.top,
          left: chipPosition.left,
          transitionMs: 1,
        };
        return params;
      }, {} as Record<string, ChipParams>)
  );

  const changeChipParams = (playerId: string, newPosition: number,
                            chipPosition: ChipPosition, transitionMs?: number) => {
    setPlayerChipParams(params => {
      const newParams = {
        ...params
      };
      newParams[playerId] = {
        ...newParams[playerId],
        endPosition: newPosition,
        top: chipPosition.top,
        left: chipPosition.left,
        transitionMs: transitionMs || moveTotalMs,
      };
      return newParams;
    });
  };

  if (chipMove
    && !playerStates[chipMove.playerId].bankrupt
    && playerChipParams[chipMove.playerId].endPosition !== chipMove.field) {
    const playerId = chipMove.playerId;
    const newPosition = chipMove.field;
    const playerArrayIndex = Object.keys(playerChipParams).indexOf(playerId);
    if (!chipMove.forward) {
      const chipPosition = defineChipPosition(newPosition, playerArrayIndex);
      changeChipParams(playerId, newPosition, chipPosition);
    } else {
      const moveBreakpoints = calculatePathForward(playerChipParams[playerId].endPosition, newPosition);
      let delay = 0;
      for (let i = 0; i < moveBreakpoints.length; i++) {
        const { index, transitionMs } = moveBreakpoints[i];
        const chipPosition = i === moveBreakpoints.length - 1
          ? defineChipPosition(index, playerArrayIndex)
          : defineChipPosition(index);
        setTimeout(() => changeChipParams(playerId, newPosition, chipPosition, transitionMs), delay);
        delay = transitionMs;
      }
    }
  }

  return (
    <div className="chips-container">
      {
        Object.entries(playerStates)
          .filter(([_, { bankrupt }]) => !bankrupt)
          .map(([playerId, _]) => {
              const { color, top, left, transitionMs } = playerChipParams[playerId];
              return (
                <PlayerChip
                  key={`${playerId}-chip`}
                  color={color}
                  top={top}
                  left={left}
                  transitionMs={transitionMs}
                />)
            }
          )
      }
    </div>
  );
}

export default PlayerChipsContainer;
