import { GameState, PlayerState, PropertyManagementActions } from "../types/interfaces";
import { UPropertyIndex } from "../types/unions";
import { useGameState } from "../context/GameStateProvider";
import { PROPERTY_FIELDS_DATA } from "../constants";
import { PROPERTY_GROUPS } from "../constants/mapData";
import { MAX_HOUSES } from "../constants/rules";
import { getLoggedInUserId } from "../utils/auth";
import { isTurnStartStage, isStageAllowsPropertyManagement } from "../utils/property";
import { useMemo } from "react";
import { PropertyGroup } from "../types/enums";

const defaultPropertyActions: PropertyManagementActions = {
  showMortgage: false,
  showRedeem: false,
  showBuyHouse: false,
  showSellHouse: false,
}

const defineAvailableActions = (
  fieldIndex: UPropertyIndex,
  loggedInUserId: string,
  gameState: GameState,
  housePurchases: PropertyGroup[],
): PropertyManagementActions => {
  const managementActions = {
    ...defaultPropertyActions
  };

  const propertyState = gameState.propertyStates[fieldIndex];
  const housesAmount: number = propertyState.houses ?? 0;
  const gameStage = gameState.stage;
  const ownerState: PlayerState = gameState.playerStates[loggedInUserId];
  const propertyPrice = PROPERTY_FIELDS_DATA[fieldIndex].price;
  if (propertyState.isMortgaged && propertyPrice && ownerState.money >= propertyPrice * 0.55) {
    managementActions.showRedeem = true;
  }

  const propertyGroup = PROPERTY_FIELDS_DATA[fieldIndex].group;
  const housePrice = PROPERTY_FIELDS_DATA[fieldIndex].housePrice;
  const groupFieldsStates = PROPERTY_GROUPS[propertyGroup].map(
    index => gameState.propertyStates[index]
  );

  if (!propertyState.isMortgaged &&
    (!housePrice || groupFieldsStates.every(state => !state.houses))) {
    managementActions.showMortgage = true;
  }

  if (housePrice) {
    if (housesAmount && groupFieldsStates.every(state => state.houses <= housesAmount)) {
      managementActions.showSellHouse = true;
    }

    if (!propertyState.isMortgaged
      && isTurnStartStage(gameStage)
      && housesAmount < MAX_HOUSES
      && !housePurchases.includes(propertyGroup)
      && groupFieldsStates
        .filter(state => state !== propertyState)
        .every(state => !state.isMortgaged
          && state.ownerId && state.ownerId === loggedInUserId
          && state.houses >= housesAmount)
      && ownerState.money >= housePrice) {
      managementActions.showBuyHouse = true;
    }
  }

  return managementActions;
}

/**
 * should be used only inside GameStateProvider
 */
const usePropertyActions = (fieldIndex: UPropertyIndex) => {

  const { gameState, housePurchases } = useGameState();

  const loggedInUserId = useMemo(getLoggedInUserId, []);
  const currentPlayerId = gameState.currentUserId;
  const propertyOwner = gameState.propertyStates[fieldIndex].ownerId;
  const gameStage = gameState.stage;

  const canManage = useMemo(() => loggedInUserId === currentPlayerId
      && loggedInUserId === propertyOwner
      && isStageAllowsPropertyManagement(gameStage),
    [
      currentPlayerId,
      propertyOwner,
      gameStage,
    ]
  );

  const availableActions = useMemo(
    () => canManage
      ? defineAvailableActions(fieldIndex, loggedInUserId, gameState, housePurchases)
      : defaultPropertyActions,
    [canManage]);

  return { canManage, availableActions };
}

export default usePropertyActions;
