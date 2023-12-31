import { UPropertyIndex } from "./unions";

interface FieldState {
  id: UPropertyIndex,
  houses: number,
  ownerId?: string,
  mortgaged: boolean,
  priceTag: string,
}

export interface GameMapRefreshEvent {
  players: {
    id: string,
    name: string,
    avatar: string,
    money: number,
    position: number,
    bankrupt: boolean,
  }[],
  fields: FieldState[],
  currentPlayer: string,
}

export interface BuyProposalEvent {
  playerId: string,
  fieldIndex: UPropertyIndex,
  price: number,
}

export interface AuctionRaiseProposalEvent {
  fieldIndex: UPropertyIndex,
  proposal: number,
}

export interface AuctionBuyProposalEvent {
  fieldIndex: UPropertyIndex,
  proposal: number,
}

export interface MoneyChangeEvent {
  changes: {
    playerId: string,
    money: number,
  }[],
}

export interface FieldChangeEvent {
  changes: FieldState[],
}

export interface PayCommandEvent {
  playerId: string,
  sum: number,
  wiseToGiveUp: boolean,
}

export interface OfferProposalEvent {
  initiatorName: string;
  addresseeFields: UPropertyIndex[],
  initiatorFields: UPropertyIndex[],
  addresseeMoney: number;
  initiatorMoney: number;
}
