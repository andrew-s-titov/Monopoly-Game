import { createContext, Dispatch, PropsWithChildren, ReactNode, SetStateAction, useContext, useState } from "react";

import { ConnectedPlayer, GameState } from "../types/interfaces";
import { INITIAL_GAME_STATE } from "../constants/mapData";
import { PropertyGroup } from "../types/enums";

interface IGameContextProps extends PropsWithChildren {
  gameId: string,
}

interface IGameStateContext {
  gameId: string,
  connectedPlayers: ConnectedPlayer[];
  gameState: GameState;
  setConnectedPlayers: Dispatch<SetStateAction<ConnectedPlayer[]>>;
  setGameState: Dispatch<SetStateAction<GameState>>;
  messages: ReactNode[];
  addChatMessage: (chatMessage: ReactNode) => void;
  housePurchases: PropertyGroup[];
  addHousePurchase: (group: PropertyGroup) => void;
  clearHousePurchaseRecords: () => void;
}

const GameStateContext = createContext<IGameStateContext>({} as IGameStateContext);

export const GameStateProvider = ({ gameId, children }: IGameContextProps) => {

  const [connectedPlayers, setConnectedPlayers] = useState<ConnectedPlayer[]>([]);
  const [messages, setMessages] = useState<ReactNode[]>([]);
  const [gameState, setGameState] = useState<GameState>(INITIAL_GAME_STATE);
  const [housePurchases, setHousePurchases] = useState<PropertyGroup[]>([]);

  const addChatMessage = (messageBody: ReactNode) => setMessages(array => [...array, messageBody]);
  const addHousePurchase = (group: PropertyGroup) => {
    setHousePurchases(prevState => ([...prevState, group]));
  }
  const clearHousePurchaseRecords = () => setHousePurchases([]);

  return (
    <GameStateContext.Provider
      value={{
        gameId,
        gameState,
        setGameState,
        messages,
        connectedPlayers,
        setConnectedPlayers,
        addChatMessage,
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
