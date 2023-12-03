import { UPropertyIndex } from "./unions";

interface FieldView {
  id: UPropertyIndex,
  houses: number,
  ownerId?: string,
  mortgage: boolean,
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
  fields: FieldView[],
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
  changes: FieldView[],
}

export interface PayCommandEvent {
  playerId: string,
  sum: number,
  wiseToGiveUp: boolean,
}

export interface MortgageChangeEvent {
  changes: {
    fieldIndex: UPropertyIndex,
    turns: number,
  }[],
}

export interface HouseAmountEvent {
  fieldIndex: UPropertyIndex,
  houses: number,
}

export interface OfferProposalEvent {
  initiatorName: string;
  addresseeFields: UPropertyIndex[],
  initiatorFields: UPropertyIndex[],
  addresseeMoney: number;
  initiatorMoney: number;
}
