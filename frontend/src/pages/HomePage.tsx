import { memo, useState } from "react";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";

import { getLandingPageWebsocketUrl } from "../config/api";
import useQuery from "../hooks/useQuery";
import GameRoomPlayer from "../components/GameRoomPlayer";
import StartPageBackground from "../components/StartPageBackground";
import StartPageButton from "../components/StartPageButton";
import StartPageCenteredContent from "../components/StartPageCenteredContent";
import { AvailableGamesEvent, GameRoomParticipant } from "../types/events";

import "../assets/styles/flags.css"
import { useRouting } from "../context/Routing";
import useWebsocket from "../hooks/useWebsocket";
import { useTranslations } from "../i18n/config";

interface GameRoomEntry {
  gameId: string,
  players?: GameRoomParticipant[],
  language?: string,
}

const perPage = 5;
const createRoomPlaceholders = (amount: number) => Array.from({ length: amount },
  (_, index) => ({
    gameId: `placeholder-${index}`
  }));

const HomePage = () => {

  const { t } = useTranslations();
  const { post } = useQuery();
  const { navigateToGame } = useRouting();
  const [rooms, setRooms] = useState<GameRoomEntry[]>(() => createRoomPlaceholders(perPage));

  const { execute: createNewGame, isLoading: isCreateLoading } = post({
    url: `/game`,
    responseHandler: ({ gameId }) => navigateToGame(gameId),
  });

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
        label={t('action.join')}
        icon={'pi pi-caret-right icon'}
        onClick={() => navigateToGame(gameId)}
      />
      : null);

  useWebsocket({
    url: getLandingPageWebsocketUrl(),
    onMessage: ({ data }: MessageEvent) => {
      console.log(`websocket message is: ${data}`);
      const rooms: AvailableGamesEvent[] = JSON.parse(data).rooms;
      const roomsData: GameRoomEntry[] = rooms.map(room =>
        ({
          ...room,
        }));
      if (roomsData.length < perPage) {
        roomsData.push(...createRoomPlaceholders(perPage - roomsData.length));
      }
      setRooms(roomsData);
    },
  });

  return (
    <StartPageBackground>
      <StartPageCenteredContent>
        <div className="gr-table-overview-header">{t('title.availableRooms')}</div>
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
            className="gr-overview-players-column"
            body={playersBody}
          />
          <Column
            body={joinBody}
            align="center"
          />
        </DataTable>
        <StartPageButton
          icon={'pi pi-plus icon'}
          label={t('action.createNewGame')}
          isLoading={isCreateLoading}
          onClickHandler={() => createNewGame({
            "withTeleport": false,
          })}
          isDisabled={false}
        />
      </StartPageCenteredContent>
    </StartPageBackground>
  );
}

export default memo(HomePage);
