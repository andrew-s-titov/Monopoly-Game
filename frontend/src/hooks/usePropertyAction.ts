import { PlayerState, PropertyManagementOptions } from "../types/interfaces";
import { UPropertyIndex } from "../types/unions";
import { useGameState } from "../context/GameStateProvider";
import { PROPERTY_FIELDS_DATA } from "../constants";
import { PROPERTY_GROUPS } from "../constants/mapData";
import { MAX_HOUSES } from "../constants/rules";
import { getLoggedInUserId } from "../utils/auth";
import { isTurnStartStage, isStageAllowsPropertyManagement } from "../utils/property";

const defaultPropertyActions: PropertyManagementOptions = {
  showMortgage: false,
  showRedeem: false,
  showBuyHouse: false,
  showSellHouse: false,
}

/**
 * should be used only inside GameStateProvider
 */
const usePropertyActions = () => {

  const { gameState, housePurchases } = useGameState();

  const getManagement = (fieldIndex: UPropertyIndex): {
    canManage: boolean,
    managementOptions: PropertyManagementOptions
  } => {
    const managementOptions = {
      ...defaultPropertyActions
    };

    const loggedInUserId = getLoggedInUserId();
    const currentPlayerId = gameState.currentUserId;
    const currentPropertyState = gameState.propertyStates[fieldIndex];
    const currentPropertyHouses: number = currentPropertyState.houses ?? 0;
    const currentGameStage = gameState.stage;

    if (loggedInUserId !== currentPlayerId
      || loggedInUserId !== currentPropertyState.ownerId
      || !isStageAllowsPropertyManagement(currentGameStage)) {
      return { canManage: false, managementOptions };
    }

    const ownerState: PlayerState = gameState.playerStates[loggedInUserId];
    const propertyPrice = PROPERTY_FIELDS_DATA[fieldIndex].price;
    if (currentPropertyState.isMortgaged && propertyPrice && ownerState.money >= propertyPrice * 0.55) {
      managementOptions.showRedeem = true;
    }

    const propertyGroup = PROPERTY_FIELDS_DATA[fieldIndex].group;
    const housePrice = PROPERTY_FIELDS_DATA[fieldIndex].housePrice;
    const groupFieldsStates = PROPERTY_GROUPS[propertyGroup].map(
      index => gameState.propertyStates[index]
    );

    if (!currentPropertyState.isMortgaged &&
      (!housePrice || groupFieldsStates.every(state => !state.houses))) {
      managementOptions.showMortgage = true;
    }

    if (housePrice) {
      if (currentPropertyHouses && groupFieldsStates.every(state => state.houses <= currentPropertyHouses)) {
        managementOptions.showSellHouse = true;
      }

      if (!currentPropertyState.isMortgaged
        && isTurnStartStage(currentGameStage)
        && currentPropertyHouses < MAX_HOUSES
        && !housePurchases.includes(propertyGroup)
        && groupFieldsStates
          .filter(state => state !== currentPropertyState)
          .every(state => !state.isMortgaged
            && state.ownerId && state.ownerId === loggedInUserId
            && state.houses >= currentPropertyHouses)
        && ownerState.money >= housePrice) {
        managementOptions.showBuyHouse = true;
      }
    }

    return {
      canManage: Object.values(managementOptions)
        .reduce((acc, condition) => acc || condition, false),
      managementOptions
    };
  }

  return { getManagement };
}

export default usePropertyActions;