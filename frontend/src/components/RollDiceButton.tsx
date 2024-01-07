import { memo } from "react";

import useQuery from "../hooks/useQuery";
import { BE_ENDPOINT } from "../api/config";
import { Button } from "primereact/button";
import { useEventModalContext } from "../context/EventModalProvider";
import { ModalId } from "./modals";

const RollDiceButton = () => {

  const { closeEventModal } = useEventModalContext();
  const { put } = useQuery();

  const onRollClick = () => {
    put({
      url: `${BE_ENDPOINT}/game/turn`,
      onSuccess: () => closeEventModal(ModalId.ROLL_DICE),
    });
  }

  return (
    <Button
      className="roll-dice-button"
      icon="pi pi-spin pi-box icon"
      label="Roll the dice!"
      severity="secondary"
      text
      raised
      onClick={onRollClick}
    />);
}

export default memo(RollDiceButton);
