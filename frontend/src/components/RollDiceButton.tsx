import { memo } from "react";

import useQuery from "../hooks/useQuery";
import { Button } from "primereact/button";
import { useEventModalContext } from "../context/EventModalProvider";
import { ModalId } from "./modals";
import { useGameState } from "../context/GameStateProvider";
import { useTranslations } from "../i18n/config";

const RollDiceButton = () => {

  const { t } = useTranslations();
  const { gameId } = useGameState();
  const { closeEventModal } = useEventModalContext();
  const { put } = useQuery();
  const { execute: rollDice } = put({
    url: `/game/${gameId}/turn`,
    onSuccess: () => closeEventModal(ModalId.ROLL_DICE),
  });

  return (
    <Button
      className="roll-dice-button"
      icon="pi pi-spin pi-box icon"
      label={t('action.rollDice')}
      severity="secondary"
      text
      raised
      onClick={() => rollDice()}
    />);
}

export default memo(RollDiceButton);
