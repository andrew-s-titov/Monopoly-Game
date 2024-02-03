import { memo, useMemo } from 'react';
import { Message } from "primereact/message";
import { useGameState } from "../../context/GameStateProvider";
import { getLoggedInUserId } from "../../utils/auth";

interface IProps {
  message: string,
  playerId: string,
}

const PlayerChatMessage = ({message, playerId}: IProps) => {

  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { gameState } = useGameState();
  const playerStates = gameState.playerStates;
  const color = playerStates[playerId].color;

  const textContent = <span>{message}</span>
  if (loggedInUser === playerId) {
    return <Message
      className='chat-message own-message'
      content={textContent}
      style={{background: color}}
    />
  } else {
    const content = (
      <>
        <span className="chat-message-author">{`${playerStates[playerId].name}: `}</span>
        {textContent}
      </>
    );
    return (
      <Message
        className="chat-message player-message"
        content={content}
        style={{background: color}}
      />
    )
  }
}

export default memo(PlayerChatMessage);
