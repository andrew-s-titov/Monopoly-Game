import { createContext, PropsWithChildren, useContext, useEffect, useRef } from "react";

import { getWebsocketUrl } from "../api/config";
import { ChatMessageBody, PlayerState, PropertyState } from "../types/interfaces";
import {
  AuctionBuyProposalEvent,
  AuctionRaiseProposalEvent,
  BuyProposalEvent,
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

interface IWebsocketContext {
  sendMessage: (chatMessage: string) => void;
}

const WebsocketContext = createContext<IWebsocketContext>({} as IWebsocketContext);

const WebsocketConnectionProvider = ({ children }: PropsWithChildren) => {

  const {
    setGameState, setConnectedPlayers, addChatMessage, clearGameState,
    clearHousePurchaseRecords
  } = useGameState();
  const { openEventModal, closeEventModal } = useEventModalContext();
  const { showCenterPopUp } = useMessageContext();

  const websocket = useRef<WebSocket>();

  const changeCurrentPlayer = (currentPlayerId: string) => {
    setGameState(prevState => ({
      ...prevState,
      currentUserId: currentPlayerId,
    }))
  };

  useEffect(() => {
      websocket.current = new WebSocket(getWebsocketUrl());
      const timeouts: ReturnType<typeof setTimeout>[] = [];

      const newTimeout = (action: () => void, delay: number) => timeouts.push(setTimeout(action, delay));
      const clearTimeouts = () => timeouts.forEach(timer => clearTimeout(timer));

      const codeActions: Record<number, (data: any) => void> = {
        100: ({ players }) => {
          setConnectedPlayers(players);
        },
        200: (message: ChatMessageBody) => {
          addChatMessage(message);
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
            header: <RollDiceButton/>,
            blurBackground: false,
          });
        },
        302: () => {
          openEventModal({
            modalId: ModalId.DICE,
            header: <Dice/>,
            blurBackground: false,
            isTransparent: true,
          })
        },
        303: ({ firstDice, secondDice }) => {
          openEventModal({
            modalId: ModalId.DICE,
            header: <Dice result={[firstDice, secondDice]}/>,
            blurBackground: false,
            isTransparent: true,
          })
          newTimeout(() => closeEventModal(ModalId.DICE), 1500);
        },
        304: ({ playerId, field }: any) => {
          setGameState(prevState => {
            const newState = {
              ...prevState
            }
            newState.playerStates[playerId].position = field;
            return newState;
          });
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
            header:
              <div className='modal-title'>
                {`Do you want to buy ${fieldName} for $${price}?`}
              </div>,
            content:
              <BuyProposalModal
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
            header:
              <div className='modal-title'>
                Choose a way out:
              </div>,
            content:
              <JailReleaseModal />,
            blurBackground: false,
          });
        },
        309: ({ fieldIndex, proposal }: AuctionRaiseProposalEvent) => {
          const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
          openEventModal({
            modalId: ModalId.AUCTION,
            header:
              <div>
                {`Do you want to raise ${fieldName} price to $${proposal}?`}
              </div>,
            content:
              <AuctionModal
                proposal={proposal}
              />,
            blurBackground: false,
          });
        },
        310: ({ fieldIndex, proposal }: AuctionBuyProposalEvent) => {
          const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
          openEventModal({
            modalId: ModalId.AUCTION_BUY_PROPOSAL,
            header:
              <div>
                {`Do you want to buy ${fieldName} for $${proposal}?`}
              </div>,
            content:
              <AuctionBuyProposalModal
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
          openEventModal(
            {
              modalId: ModalId.PAY_COMMAND,
              header:
                <div>
                  {`Pay $${sum}`}
                </div>,
              content:
                <PayCommandModal
                  sum={sum}
                  wiseToGiveUp={wiseToGiveUp}
                />,
              blurBackground: false,
            }
          );
        },
        313: ({ text }) => {
          showCenterPopUp(<ChanceCard text={text}/>);
        },
        314: ({ playerId }) => {
          changeCurrentPlayer(playerId);
          clearHousePurchaseRecords();
        },
        315: ({ winnerName }) => {
          openEventModal({
            modalId: ModalId.WINNER,
            header: <WinnerModal name={winnerName}/>,
            blurBackground: true,
          });
          changeCurrentPlayer('');
          clearTimeouts();
          newTimeout(
            () => {
              closeEventModal();
              clearGameState();
            },
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
            header:
              <div className="offer-title">
                {`${initiatorName} made you an offer:`}
              </div>,
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
      }

      websocket.current.onmessage = ({ data }: MessageEvent) => {
        console.log(`websocket message is: ${data}`);
        const messageData = JSON.parse(data);
        const socketMessageCode = messageData.code;
        codeActions[socketMessageCode](messageData);
      };

      websocket.current.onclose = (event: CloseEvent) => {
        console.log(`websocket close event: ${JSON.stringify({
          code: event.code,
          reason: event.reason,
        })}`);
      }

      return () => {
        clearTimeouts();
        websocket.current
        && websocket.current?.readyState === websocket.current?.OPEN
        && websocket.current.close(1000, 'player left the game room / the game');
      }
    }, []
  );

  const sendMessage = (chatMessage: string) => {
    if (websocket.current && websocket.current.readyState === websocket.current.OPEN) {
      websocket.current.send(chatMessage);
    }
  }

  return (
    <WebsocketContext.Provider value={{ sendMessage }}>
      {children}
    </WebsocketContext.Provider>
  );
}

export default WebsocketConnectionProvider;

export const useWebsocketContext = () => useContext(WebsocketContext);
