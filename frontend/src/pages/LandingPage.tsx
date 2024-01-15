import { memo, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";

import { BE_ENDPOINT, getLandingPageWebsocketUrl } from "../config/api";
import useQuery from "../hooks/useQuery";
import GameRoomPlayer from "../components/GameRoomPlayer";
import StartPageBackground from "../components/StartPageBackground";
import StartPageButton from "../components/StartPageButton";
import StartPageCenteredContent from "../components/StartPageCenteredContent";
import { GameRoomParticipant } from "../types/events";

import "../assets/styles/flags.css"

interface GameRoomEntry {
  gameId?: string,
  players?: GameRoomParticipant[],
  language?: string,
}

const perPage = 5;

const LandingPage = () => {

  // TODO: on load - useEffect for finding player active games

  const websocket = useRef<WebSocket>();
  const { post } = useQuery();
  const navigate = useNavigate();
  const [rooms, setRooms] = useState<GameRoomEntry[]>([]);

  const { execute: createNewGame, isLoading: isCreateLoading } = post({
    url: `${BE_ENDPOINT}/game/new`,
    responseHandler: () => navigate('/game'),
    // responseHandler: ({ gameId }) => navigate(`/game/${gameId}`), TODO: enable for multi-room setup
  });

  const languageBody = ({ language }: GameRoomEntry) =>
    language && <div className={`flag flag-${language}`}></div>;
  const playersBody = ({ players }: GameRoomEntry) =>
    (players && <div className="gr-players-container">
      {players.map(({ name, avatar }) =>
        <GameRoomPlayer
          key={name}
          name={name}
          avatar={avatar}
          isOverview
        />
      )}
    </div>);
  const joinBody = ({ gameId }: GameRoomEntry) =>
    (gameId && !gameId.includes('placeholder')
      ?
      <Button
        text
        className="join-button"
        severity="secondary"
        label="Join"
        icon={'pi pi-caret-right icon'}
        // onClick={() => navigate(`/game/${gameId}`)}
        onClick={() => navigate('/game')}
      />
      : null);

  useEffect(() => {
      websocket.current = new WebSocket(getLandingPageWebsocketUrl());

      websocket.current.onmessage = ({ data }: MessageEvent) => {
        console.log(`websocket message is: ${data}`);
        const rooms: Record<string, GameRoomParticipant[]> = JSON.parse(data).rooms;
        const roomsData: GameRoomEntry[] = Object.entries(rooms)
          .map(([gameId, players]) =>
            ({
              gameId,
              players,
              language: 'en',
            })
          );
        if (roomsData.length < perPage) {
          roomsData.push(...Array.from({ length: perPage - roomsData.length },
            (_, index) => ({
              gameId: `placeholder-${index}`
            })));
        }
        setRooms(roomsData);
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
      <StartPageCenteredContent>
        <div className="gr-table-overview-header">Available game rooms</div>
        <DataTable
          value={rooms}
          showHeaders={false}
          dataKey="gameId"
          paginator
          rows={perPage}
          tableClassName="gr-overview-table"
          rowClassName={() => 'gr-overview-row'}
        >
          <Column
            field="players"
            header="Players"
            className="gr-overview-players-column"
            body={playersBody}
          />
          <Column
            field="language"
            body={languageBody}
            align="center"
          />
          <Column
            body={joinBody}
            align="center"
          />
        </DataTable>
        <StartPageButton
          icon={'pi pi-plus icon'}
          label='Create new'
          isLoading={isCreateLoading}
          onClickHandler={() => createNewGame({})}
          isDisabled={false}
        />
      </StartPageCenteredContent>
    </StartPageBackground>
  );
}

export default memo(LandingPage);
