import { memo, startTransition, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import { BE_ENDPOINT, getLandingPageWebsocketUrl } from "../config/api";
import useQuery from "../hooks/useQuery";
import StartPageBackground from "../components/StartPageBackground";
import StartPageButton from "../components/StartPageButton";
import { GameRoomParticipant } from "../types/events";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import GameRoomPlayer from "../components/GameRoomPlayer";

import "../assets/styles/flags.css"

const LandingPage = () => {

  // TODO: useEffect for finding player active games

  const websocket = useRef<WebSocket>();
  const { post } = useQuery();
  const navigate = useNavigate();
  const [rooms, setRooms] = useState<Record<string, GameRoomParticipant[]>>({});

  const { execute: createNewGame, isLoading: isCreateLoading } = post({
    url: `${BE_ENDPOINT}/game/new`,
    responseHandler: () => navigate(`/game`),
    // responseHandler: ({ gameId }) => navigate(`/game/${gameId}`), TODO: enable for multi-room setup
  });

  const tableData = Object.entries(rooms)
    .map(([gameId, players]) =>
      ({
        gameId,
        players:
          <div className="gr-players-container">
            {players.map(
              ({ name, avatar }) =>
                <GameRoomPlayer
                  key={name}
                  name={name}
                  avatar={avatar}
                  isOverview
                />
            )}
          </div>,
        language:
          <div className={`flag flag-gb`}></div>
      })
    )
  ;

  useEffect(() => {
      websocket.current = new WebSocket(getLandingPageWebsocketUrl());

      websocket.current.onmessage = ({ data }: MessageEvent) => {
        console.log(`websocket message is: ${data}`);
        const rooms: Record<string, GameRoomParticipant[]> = JSON.parse(data).rooms;
        setRooms(rooms);
      };

      websocket.current.onclose = (event: CloseEvent) => {
        console.log(`websocket closed: ${JSON.stringify({
          code: event.code,
          reason: event.reason,
        })}`);
      }

      return () => {
        websocket.current
        && websocket.current?.readyState === websocket.current?.OPEN
        && websocket.current.close(1000, 'player left the page');
      }
    }, []
  );

  return (
    <StartPageBackground>
      <StartPageButton
        icon={'pi pi-plus icon'}
        label='Create new'
        isLoading={isCreateLoading}
        onClickHandler={() => createNewGame({})}
        isDisabled={false}
      />
      <DataTable value={tableData} dataKey="gameId">
        <Column field="players" header="Players" className="gr-overview-players-column"/>
        <Column field="language" header="Language"/>
      </DataTable>
    </StartPageBackground>
  );
}

export default memo(LandingPage);
