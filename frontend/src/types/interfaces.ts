import { UPropertyIndex } from "./unions";
import { PropertyGroup } from "./enums";

export interface ChatMessageBody {
  message: string;
  playerId?: string;
}

export interface PropertyStaticData {
  name: string;
  group: PropertyGroup;
  price: number;
  housePrice?: number;
}

export interface PropertyManagementOptions {
  showMortgage: boolean;
  showRedeem: boolean;
  showBuyHouse: boolean;
  showSellHouse: boolean;
}

export interface PropertyState {
  isMortgaged: boolean;
  priceTag: string;
  houses: number;
  ownerId?: string;
}

export interface PlayerState {
  name: string;
  color: string;
  money: number;
  bankrupt: boolean;
  position: number;
}

export interface GameState {
  stage: string;
  currentUserId: string;
  propertyStates: Record<UPropertyIndex, PropertyState>;
  playerStates: Record<string, PlayerState>;
  gameStarted: boolean,
}

export interface Deal {
  initiatorFields: UPropertyIndex[],
  addresseeFields: UPropertyIndex[],
  initiatorMoney: number;
  addresseeMoney: number;
}

export interface PropertyShortInfo {
  fieldIndex: UPropertyIndex,
  name: string,
  group: PropertyGroup,
}

export interface ConnectedPlayer {
  playerId: string,
  name: string,
}

export interface AuthData {
  player_id: string,
  player_name: string,
}