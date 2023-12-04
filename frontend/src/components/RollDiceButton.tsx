import { memo } from "react";

import useQuery from "../hooks/useQuery";
import { useEventModalContext } from "../context/EventModalProvider";
import { BE_ENDPOINT } from "../api/config";
import { Button } from "primereact/button";

const RollDiceButton = () => {

  const { get } = useQuery();
  const { closeEventModal } = useEventModalContext();

  const onRollClick = () => {
    get({
      url: `${BE_ENDPOINT}/game/dice/roll`,
    });
    closeEventModal();
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
