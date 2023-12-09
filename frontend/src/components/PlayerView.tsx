import { CSSProperties, useCallback, useMemo, useRef } from "react";

import { useGameState } from "../context/GameStateProvider";
import { OverlayPanel } from "primereact/overlaypanel";
import { Button } from "primereact/button";
import { GiveUpModal, OfferDealModal } from "./modals";
import { getLoggedInUserId } from "../utils/auth";
import { usePopUpModalContext } from "../context/PopUpModalProvider";
import { isTurnStartStage } from "../utils/property";

interface IPlayerViewProps {
  playerId: string
}

const PlayerView = ({ playerId }: IPlayerViewProps) => {

  const { openPopUpModal } = usePopUpModalContext();
  const playerManagementOverlay = useRef<OverlayPanel>(null);
  const loggedInUser = useMemo(getLoggedInUserId, []);
  const { gameState } = useGameState();
  const playerState = gameState.playerStates[playerId];
  const playerMoney = `$ ${playerState ? playerState.money : '0'}`;
  const playerColor = playerState ? playerState.color : 'white';
  const playersTurnClass = gameState.currentUserId === playerId ? 'flashing-icon' : '';
  const viewStyle = playerState.bankrupt ? 'bankrupt-player-icon' : 'active-player-icon';
  const isGiveUpOnClickAvailable = loggedInUser === playerId;
  const isOfferDealOnClickAvailable = loggedInUser !== playerId
    && gameState.currentUserId === loggedInUser
    && isTurnStartStage(gameState.stage);
  const shouldRenderOverlayOnClick = !playerState.bankrupt
    && (isGiveUpOnClickAvailable || isOfferDealOnClickAvailable);

  const hideManagementButton = useCallback(
    () => playerManagementOverlay.current?.hide(),
    []);

  const onPlayerGiveUp = useCallback(() => {
    hideManagementButton();
    openPopUpModal(
      {
        header:
          <div className='modal-title'>
            Are you sure you want to give up?
          </div>,
        modalContent: <GiveUpModal/>,
      });
  }, []);

  const onOfferDeal = useCallback(() => {
    hideManagementButton();
    openPopUpModal(
      {
        header:
          <div className='offer-title'>
            Choose fields to buy or sell and enter money to exchange:
          </div>,
        modalContent: <OfferDealModal addresseeId={playerId}/>,
      });
  }, []);

  return (
    <div className='player-view'>
      <div
        className='player-icon-container'
        onClick={(e) => shouldRenderOverlayOnClick && playerManagementOverlay.current?.toggle(e)}
      >
        {!playerState.bankrupt && <div
          className={`player-icon-outline`}
          style={
            {
              "--player-color": `${playerColor}`
            } as CSSProperties
          }
        ></div>}
        <div className={`${viewStyle} ${playersTurnClass}`}></div>

        <OverlayPanel
          ref={playerManagementOverlay}
          dismissable
          closeOnEscape
        >
          {loggedInUser === playerId
            ?
            <Button
              className='player-management-button'
              label='Give up'
              onClick={onPlayerGiveUp}
              icon='pi pi-flag modal-button-icon'
              text
            />
            :
            <Button
              className='player-management-button'
              label='Offer deal'
              onClick={onOfferDeal}
              icon='pi pi-file-edit modal-button-icon'
              text
            />
          }
        </OverlayPanel>

      </div>
      <div className='player-name'>{playerState.name}</div>
      <div className='player-money'>{playerMoney}</div>
    </div>
  );
}

export default PlayerView;
