import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useRef } from "react";

import { BE_ENDPOINT, getWebsocketUrl } from "../api/config";
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
import useQuery from "../hooks/useQuery";
import { useEventModalContext } from "./EventModalProvider";
import { getLoggedInUserId } from "../utils/auth";
import {
  AuctionBuyProposalModal,
  AuctionModal,
  BuyProposalModal,
  JailReleaseModal,
  OfferProposalModal,
  PayCommandModal
} from "../components/modals";
import WinnerModal from "../components/modals/WinnerModal";
import { IModalProps } from "../hooks/useModal";

interface IWebsocketContext {
  sendMessage: (chatMessage: ChatMessageBody) => void;
}

const WebsocketContext = createContext<IWebsocketContext>({} as IWebsocketContext);

const WebsocketConnectionProvider = ({ children }: PropsWithChildren) => {

  const {
    setGameState, setConnectedPlayers, addChatMessage, clearGameState,
    clearHousePurchaseRecords
  } = useGameState();
  const { get } = useQuery();
  const { openEventModal, closeEventModal } = useEventModalContext();

  const loggedInUserId = useMemo(getLoggedInUserId, []);
  const websocket = useRef<WebSocket>();

  const changeCurrentPlayer = (currentPlayerId: string) => {
    setGameState(prevState => ({
      ...prevState,
      currentUserId: currentPlayerId,
    }))
  };

  const openModalOnlyForLoggedInUser = (modalForPlayerId: string, modalProps: IModalProps) => {
    if (loggedInUserId === modalForPlayerId) {
      openEventModal(modalProps);
    } else {
      closeEventModal();
    }
  }

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
        301: ({ playerId }) => {
          changeCurrentPlayer(playerId);
          clearHousePurchaseRecords();
          openModalOnlyForLoggedInUser(playerId, {
              header: <RollDiceButton/>,
              draggable: false,
              modal: false,
            });
        },
        302: ({ playerId }) => {
          openEventModal({
            header: <Dice/>,
            draggable: false,
            modal: false,
            transparent: true,
          })
          loggedInUserId === playerId &&
          newTimeout(
            () => get({
              url: `${BE_ENDPOINT}/game/dice/result`
            }),
            1500);
        },
        303: ({ playerId, firstDice, secondDice }) => {
          openEventModal({
            header: <Dice result={[firstDice, secondDice]}/>,
            draggable: false,
            modal: false,
            transparent: true,
          })
          newTimeout(closeEventModal, 1500);
          newTimeout(
            () => {
              loggedInUserId === playerId && get({
                url: `${BE_ENDPOINT}/game/dice/after`
              });
            },
            2000);
        },
        304: ({ playerId, field, needAfterMoveCall }: any) => {
          setGameState(prevState => {
            const newState = {
              ...prevState
            }
            newState.playerStates[playerId].position = field;
            return newState;
          });
          needAfterMoveCall && loggedInUserId === playerId &&
          newTimeout(
            () => get({
              url: `${BE_ENDPOINT}/game/after_move`
            }),
            500);
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
        306: ({ playerId, price, fieldIndex }: BuyProposalEvent) => {
          const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
          loggedInUserId === playerId && openEventModal({
            header:
              <div className='modal-title'>
                {`Do you want to buy ${fieldName} for $${price}?`}
              </div>,
            modalContent:
              <BuyProposalModal
                playerId={playerId}
                price={price}
              />,
            modal: false,
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
        308: ({ playerId }) => {
          changeCurrentPlayer(playerId);
          clearHousePurchaseRecords();
          openModalOnlyForLoggedInUser(playerId, {
              header:
                <div className='modal-title'>
                  Choose a way out:
                </div>,
              modalContent:
                <JailReleaseModal
                  playerId={playerId}
                />,
              modal: false,
            });
        },
        309: ({ playerId, fieldIndex, proposal }: AuctionRaiseProposalEvent) => {
          const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
          openModalOnlyForLoggedInUser(playerId, {
              header:
                <div>
                  {`Do you want to raise ${fieldName} price to $${proposal}?`}
                </div>,
              modalContent:
                <AuctionModal
                  playerId={playerId}
                  proposal={proposal}
                />,
              modal: false,
            });
        },
        310: ({ playerId, fieldIndex, proposal }: AuctionBuyProposalEvent) => {
          const fieldName = PROPERTY_FIELDS_DATA[fieldIndex].name;
          openModalOnlyForLoggedInUser(playerId, {
              header:
                <div>
                  {`Do you want to buy ${fieldName} for $${proposal}?`}
                </div>,
              modalContent:
                <AuctionBuyProposalModal
                  proposal={proposal}
                />,
              modal: false,
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
        312: ({ playerId, sum, wiseToGiveUp }: PayCommandEvent) => {
          loggedInUserId === playerId && openEventModal(
            {
              header:
                <div>
                  {`Pay $${sum}`}
                </div>,
              modalContent:
                <PayCommandModal
                  playerId={playerId}
                  sum={sum}
                  wiseToGiveUp={wiseToGiveUp}
                />,
              modal: false,
            }
          );
        },
        315: ({ winnerName }) => {
          openEventModal({
            header: <WinnerModal name={winnerName}/>,
            modal: true,
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
                addresseeId,
                addresseeMoney,
                initiatorMoney,
                initiatorFields,
                addresseeFields
              }: OfferProposalEvent) => {
          openModalOnlyForLoggedInUser(addresseeId, {
            header:
              <div className="offer-title">
                {`${initiatorName} made you an offer:`}
              </div>,
            modalContent:
              <OfferProposalModal
                initiatorName={initiatorName}
                addresseeFields={addresseeFields}
                initiatorFields={initiatorFields}
                addresseeMoney={addresseeMoney}
                initiatorMoney={initiatorMoney}
              />,
            modal: true,
          });
        },
        317: () => {
        },
        318: () => {
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
        && websocket.current.close(1000, 'connection closed on React component unload');
      }
    }, []
  );

  const sendMessage = (chatMessage: ChatMessageBody) => {
    if (websocket.current && websocket.current.readyState === websocket.current.OPEN) {
      websocket.current.send(JSON.stringify(chatMessage));
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
