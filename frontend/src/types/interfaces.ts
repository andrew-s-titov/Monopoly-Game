import { UPropertyIndex } from "./unions";
import { PropertyGroup } from "./enums";

export interface LoginData {
  name: string,
  avatar: string,
}

export interface ChatMessageBody {
  message: string,
  playerId?: string,
}

export interface PropertyStaticData {
  name: string,
  group: PropertyGroup,
  price: number,
  housePrice?: number,
}

export interface PropertyManagementOptions {
  showMortgage: boolean,
  showRedeem: boolean,
  showBuyHouse: boolean,
  showSellHouse: boolean,
}

export interface PropertyState {
  isMortgaged: boolean,
  priceTag: string,
  houses: number,
  ownerId?: string,
}

export interface PlayerState {
  name: string,
  avatar: string,
  color: string,
  money: number,
  bankrupt: boolean,
  position: number,
}

export interface GameState {
  stage: string,
  currentUserId: string,
  propertyStates: Record<UPropertyIndex, PropertyState>,
  playerStates: Record<string, PlayerState>,
  gameStarted: boolean,
}

export interface Deal {
  initiatorFields: UPropertyIndex[],
  addresseeFields: UPropertyIndex[],
  initiatorMoney: number,
  addresseeMoney: number,
}

export interface PropertyShortInfo {
  fieldIndex: UPropertyIndex,
  name: string,
  group: PropertyGroup,
}

export interface ConnectedPlayer {
  id: string,
  name: string,
  avatar: string,
}

export interface LoginResponse {
  id: string,
  name: string,
  avatar: string,
}

export interface PlayerAuthData {
  id: string,
  name: string,
  avatar: string,
}
