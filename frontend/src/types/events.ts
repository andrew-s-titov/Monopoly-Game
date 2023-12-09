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
  playerId: string,
  fieldIndex: UPropertyIndex,
  proposal: number,
}

export interface AuctionBuyProposalEvent {
  playerId: string,
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
  addresseeId: string;
  addresseeFields: UPropertyIndex[],
  initiatorFields: UPropertyIndex[],
  addresseeMoney: number;
  initiatorMoney: number;
}
