import { createContext, PropsWithChildren, useContext, useRef } from "react";

import { getGameWebsocketUrl } from "../config/api";
import { ChatMessageBody, PlayerState, PropertyState, SystemMessageBody } from "../types/interfaces";
import {
  AuctionBuyProposalEvent,
  AuctionRaiseProposalEvent,
  BuyProposalEvent,
  ChipMoveEvent,
  FieldChangeEvent,
  GameMapRefreshEvent,
  MoneyChangeEvent,
  OfferProposalEvent,
  PayCommandEvent
} from "../types/events";
import { PLAYER_COLORS, PROPERTY_FIELDS_DATA } from "../constants";
import { UPropertyIndex } from "../types/unions";
import RollDiceButton from "../components/RollDiceButton";
import Dice from "../components/Dice";
import { useGameState } from "./GameStateProvider";
import { useEventModalContext } from "./EventModalProvider";
import {
  AuctionBuyProposalModal,
  AuctionModal,
  BuyProposalModal,
  JailReleaseModal,
  ModalId,
  OfferProposalModal,
  PayCommandModal,
  WinnerModal
} from "../components/modals";
import { useMessageContext } from "./MessageProvider";
import ChanceCard from "../components/ChanceCard";
import { useRouting } from "./Routing";
import useWebsocket from "../hooks/useWebsocket";
import PlayerChatMessage from "../components/chat/PlayerChatMessage";
import SystemMessage from "../components/chat/SystemMessage";

interface IGameContextProvider {
  sendMessage: (chatMessage: string) => void;
}

const ActiveGameContext = createContext<IGameContextProvider>({} as IGameContextProvider);

const ActiveGameContextProvider = ({ children }: PropsWithChildren) => {

  const { navigate } = useRouting();
  const {
    gameId, setChipMove,
    setGameState, setConnectedPlayers, addChatMessage, clearHousePurchaseRecords
  } = useGameState();
  const { openEventModal, closeEventModal } = useEventModalContext();
  const { showCenterPopUp } = useMessageContext();
  const timeouts = useRef<ReturnType<typeof setTimeout>[]>([]);
  const newTimeout = (action: () => void, delay: number) => timeouts.current.push(setTimeout(action, delay));
  const clearTimeouts = () => timeouts.current.forEach(timer => clearTimeout(timer));

  const changeCurrentPlayer = (currentPlayerId: string) => {
    setGameState(prevState => ({
      ...prevState,
      currentUserId: currentPlayerId,
    }))
  };

  const webSocketCodeActions: Record<number, (data: any) => void> = {
    100: ({ players }) => {
      setConnectedPlayers(players);
    },
    200: ({ message, playerId }: ChatMessageBody) => {
      addChatMessage(
        <PlayerChatMessage
          message={message}
          playerId={playerId}
        />
      );
    },
    201: ({ translationKey, params }: SystemMessageBody) => {
      addChatMessage(
        <SystemMessage
          translationKey={translationKey}
          params={params}
        />);
    },
    202: ({ translationKey, params }) => {
      showCenterPopUp(
        <ChanceCard
          translationKey={translationKey}
          params={params}
        />);
    },
    300: ({ players, fields, currentPlayer }: GameMapRefreshEvent) => {
      setGameState(prevState => ({
        ...prevState,
        currentUserId: currentPlayer,
        gameStarted: true,
        playerStates: players.reduce((acc, { name, id, avatar, money, bankrupt, position }, index) => {
          return {
            ...acc,
            [id]: {
              name,
              avatar,
              money,
              bankrupt,
              position,
              color: PLAYER_COLORS[index],
            },
          }
        }, {} as Record<string, PlayerState>),
        propertyStates: fields.reduce((acc, { id, houses, ownerId, mortgaged, priceTag }) => {
          return {
            ...acc,
            [id]: {
              isMortgaged: mortgaged,
              ownerId,
              houses,
              priceTag,
            },
          }
        }, {} as Record<UPropertyIndex, PropertyState>)
      }));
    },
    301: () => {
      openEventModal({
        modalId: ModalId.ROLL_DICE,
        content: <RollDiceButton/>,
        blurBackground: false,
      });
    },
    302: () => {
      openEventModal({
        modalId: ModalId.DICE,
        content: <Dice/>,
        blurBackground: false,
        isTransparent: true,
      })
    },
    303: ({ firstDice, secondDice }) => {
      openEventModal({
        modalId: ModalId.DICE,
        content: <Dice result={[firstDice, secondDice]}/>,
        blurBackground: false,
        isTransparent: true,
      })
      newTimeout(() => closeEventModal(ModalId.DICE), 1500);
    },
    304: (chipMove: ChipMoveEvent) => {
      setGameState(prevState => {
        const newState = {
          ...prevState
        }
        newState.playerStates[chipMove.playerId].position = chipMove.field;
        return newState;
      });
      setChipMove(chipMove);
    },
    305: ({ changes }: MoneyChangeEvent) => {
      setGameState(prevState => {
        const newState = {
          ...prevState,
        }
        changes.forEach(({ playerId, money }) => newState.playerStates[playerId].money = money)
        return newState;
      })
    },
    306: ({ price, fieldIndex }: BuyProposalEvent) => {
      const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
      openEventModal({
        modalId: ModalId.BUY_PROPOSAL,
        content:
          <BuyProposalModal
            fieldName={fieldName}
            price={price}
          />,
        blurBackground: false,
      });
    },
    307: ({ changes }: FieldChangeEvent) => {
      setGameState(prevState => {
        const newState = {
          ...prevState
        }
        changes.forEach(({ id, ownerId, houses, mortgaged, priceTag }) => {
          newState.propertyStates[id] = {
            ...newState.propertyStates[id],
            ownerId,
            houses,
            isMortgaged: mortgaged,
            priceTag,
          }
        })
        return newState;
      });
    },
    308: () => {
      openEventModal({
        modalId: ModalId.JAIL_RELEASE,
        content: <JailReleaseModal/>,
        blurBackground: false,
      });
    },
    309: ({ fieldIndex, proposal }: AuctionRaiseProposalEvent) => {
      const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
      openEventModal({
        modalId: ModalId.AUCTION,
        content:
          <AuctionModal
            fieldName={fieldName}
            proposal={proposal}
          />,
        blurBackground: false,
      });
    },
    310: ({ fieldIndex, proposal }: AuctionBuyProposalEvent) => {
      const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
      openEventModal({
        modalId: ModalId.AUCTION_BUY_PROPOSAL,
        content:
          <AuctionBuyProposalModal
            fieldName={fieldName}
            proposal={proposal}
          />,
        blurBackground: false,
      });
    },
    311: ({ playerId }) => {
      setGameState(prevState => {
        const newState = {
          ...prevState
        }
        newState.playerStates[playerId].bankrupt = true;
        return newState;
      })
    },
    312: ({ sum, wiseToGiveUp }: PayCommandEvent) => {
      openEventModal({
          modalId: ModalId.PAY_COMMAND,
          content:
            <PayCommandModal
              sum={sum}
              wiseToGiveUp={wiseToGiveUp}
            />,
          blurBackground: false,
        }
      );
    },
    314: ({ playerId }) => {
      changeCurrentPlayer(playerId);
      clearHousePurchaseRecords();
    },
    315: ({ winnerName }) => {
      openEventModal({
        modalId: ModalId.WINNER,
        content: <WinnerModal name={winnerName}/>,
        blurBackground: true,
      });
      changeCurrentPlayer('');
      clearTimeouts();
      newTimeout(
        () => navigate('home'),
        5000
      );
    },
    316: ({
            initiatorName,
            addresseeMoney,
            initiatorMoney,
            initiatorFields,
            addresseeFields
          }: OfferProposalEvent) => {
      openEventModal({
        modalId: ModalId.OFFER_PROPOSAL,
        content:
          <OfferProposalModal
            initiatorName={initiatorName}
            addresseeFields={addresseeFields}
            initiatorFields={initiatorFields}
            addresseeMoney={addresseeMoney}
            initiatorMoney={initiatorMoney}
          />,
        blurBackground: true,
      });
    },
    350: ({ gameStage }) => {
      setGameState(prevState => ({
        ...prevState,
        stage: gameStage,
      }));
    }
  };

  const { sendToServer } = useWebsocket(
    {
      url: getGameWebsocketUrl(gameId),
      onMessage: ({ data }: MessageEvent) => {
        console.log(`websocket message is: ${data}`);
        const messageData = JSON.parse(data);
        const socketMessageCode = messageData.code;
        webSocketCodeActions[socketMessageCode](messageData);
      },
      onDestroy: () => clearTimeouts(),
    }
  );

  const sendMessage = (chatMessage: string) => sendToServer(chatMessage);

  return (
    <ActiveGameContext.Provider value={{ sendMessage }}>
      {children}
    </ActiveGameContext.Provider>
  );
}

export default ActiveGameContextProvider;

export const useActiveGameContext = () => useContext(ActiveGameContext);
