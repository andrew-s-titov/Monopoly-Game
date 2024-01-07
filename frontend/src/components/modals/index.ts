import AuctionBuyProposalModal from "./AuctionBuyProposalModal";
import AuctionModal from "./AuctionModal";
import BuyProposalModal from "./BuyProposalModal";
import GiveUpModal from "./GiveUpModal";
import JailReleaseModal from "./JailReleaseModal";
import OfferDealModal from "./OfferDealModal";
import OfferProposalModal from "./OfferProposalModal";
import PayCommandModal from "./PayCommandModal";
import PropertyManagementModal from "./PropertyManagementModal";
import WinnerModal from "./WinnerModal";

enum ModalId {
  AUCTION_BUY_PROPOSAL = 'auction_buy_proposal',
  AUCTION = 'auction',
  BUY_PROPOSAL = 'buyProposal',
  JAIL_RELEASE = 'jailRelease',
  OFFER_PROPOSAL = 'offerProposal',
  PAY_COMMAND = 'payCommand',
  WINNER = 'winner',
  ROLL_DICE = 'rollDice',
  DICE = 'dice',
  GIVE_UP = 'giveUp',
  OFFER_DEAL = 'offerDeal',
  PROPERTY_MANAGEMENT = 'propertyManagement',
}

export {
  AuctionBuyProposalModal,
  AuctionModal,
  BuyProposalModal,
  GiveUpModal,
  JailReleaseModal,
  OfferDealModal,
  OfferProposalModal,
  PayCommandModal,
  PropertyManagementModal,
  WinnerModal,
  ModalId
}
