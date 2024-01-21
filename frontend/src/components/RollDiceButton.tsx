import { memo } from "react";

import useQuery from "../hooks/useQuery";
import { gameBaseUrl } from "../config/api";
import { Button } from "primereact/button";
import { useEventModalContext } from "../context/EventModalProvider";
import { ModalId } from "./modals";
import { useGameState } from "../context/GameStateProvider";

const RollDiceButton = () => {

  const { gameId } = useGameState();
  const { closeEventModal } = useEventModalContext();
  const { put } = useQuery();
  const { execute: rollDice } = put({
    url: `${gameBaseUrl(gameId)}/turn`,
    onSuccess: () => closeEventModal(ModalId.ROLL_DICE),
  });

  return (
    <Button
      className="roll-dice-button"
      icon="pi pi-spin pi-box icon"
      label="Roll the dice!"
      severity="secondary"
      text
      raised
      onClick={() => rollDice()}
    />);
}

export default memo(RollDiceButton);
