import { FieldPosition } from "../types/enums";
import { GameStages } from "../constants";

const PROPERTY_FIELDS_CLASSNAMES: Record<FieldPosition, string> = {
  [FieldPosition.top]: 'vertical-field property-top',
  [FieldPosition.bottom]: 'vertical-field property-bottom',
  [FieldPosition.left]: 'horizontal-field property-left ',
  [FieldPosition.right]: 'horizontal-field property-right',
  [FieldPosition.corner]: 'map-corner',
}

export const isStageAllowsPropertyManagement = (gameStage: string): boolean => {
  return [
    GameStages.TURN_START,
    GameStages.JAIL_RELEASE_START,
    GameStages.AWAITING_PAYMENT,
    GameStages.AWAITING_JAIL_FINE,
    GameStages.BUY_PROPOSAL,
  ].includes(gameStage);
}

export const isStageAllowsBuyHouse = (gameStage: string): boolean => {
  return [
    GameStages.TURN_START,
    GameStages.JAIL_RELEASE_START,
  ].includes(gameStage);
}

export const defineFieldClassname = (fieldPosition: FieldPosition) => {
  return `map-field ${PROPERTY_FIELDS_CLASSNAMES[fieldPosition]}`;
}