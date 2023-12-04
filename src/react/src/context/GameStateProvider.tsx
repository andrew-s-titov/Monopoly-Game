import { createContext, Dispatch, PropsWithChildren, SetStateAction, useContext, useState } from "react";

import { ChatMessageBody, ConnectedPlayer, GameState } from "../types/interfaces";
import { INITIAL_GAME_STATE } from "../constants/mapData";
import { PropertyGroup } from "../types/enums";

interface IGameStateContext {
  connectedPlayers: ConnectedPlayer[];
  gameState: GameState;
  setConnectedPlayers: Dispatch<SetStateAction<ConnectedPlayer[]>>;
  setGameState: Dispatch<SetStateAction<GameState>>;
  messages: ChatMessageBody[];
  addChatMessage: (chatMessage: ChatMessageBody) => void;
  clearGameState: () => void;
  housePurchases: PropertyGroup[];
  addHousePurchase: (group: PropertyGroup) => void;
  clearHousePurchaseRecords: () => void;
}

const GameStateContext = createContext<IGameStateContext>({} as IGameStateContext);

export const GameStateProvider = ({ children }: PropsWithChildren) => {

  const [connectedPlayers, setConnectedPlayers] = useState<ConnectedPlayer[]>([]);
  const [messages, setMessages] = useState<ChatMessageBody[]>([]);
  const [gameState, setGameState] = useState<GameState>(INITIAL_GAME_STATE);
  const [housePurchases, setHousePurchases] = useState<PropertyGroup[]>([]);

  const addChatMessage = (message: ChatMessageBody) => setMessages(array => [...array, message]);
  const addHousePurchase = (group: PropertyGroup) => {
    setHousePurchases(prevState => ([...prevState, group]));
  }
  const clearHousePurchaseRecords = () => setHousePurchases([]);

  const clearGameState = () => {
    setGameState(INITIAL_GAME_STATE);
    setMessages([]);
    clearHousePurchaseRecords();
  };

  return (
    <GameStateContext.Provider
      value={{
        gameState,
        setGameState,
        messages,
        connectedPlayers,
        setConnectedPlayers,
        addChatMessage,
        clearGameState,
        housePurchases,
        addHousePurchase,
        clearHousePurchaseRecords,
      }}
    >
      {children}
    </GameStateContext.Provider>
  );
}

export const useGameState = () => useContext(GameStateContext);
