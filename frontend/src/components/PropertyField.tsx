import { useMemo } from 'react';

import { UPropertyIndex } from '../types/unions';
import { FieldOrientation, FieldPosition } from '../types/enums';
import HouseContainer from './HouseContainer';
import { PropertyState } from '../types/interfaces';
import { PROPERTY_FIELDS_DATA } from '../constants';
import { defineFieldClassname } from '../utils/property';
import { MAX_HOUSES } from '../constants/rules';
import MortgageTag from "./MortgageTag";
import PriceTag from "./PriceTag";
import { useGameState } from "../context/GameStateProvider";
import OwnerCover from "./OwnerCover";
import { PropertyManagementModal } from "./modals";
import { usePopUpModalContext } from "../context/PopUpModalProvider";

interface IFieldProps {
  position: FieldPosition;
  index: UPropertyIndex;
}

const PropertyField = ({ position, index }: IFieldProps) => {

  const { openPopUpModal } = usePopUpModalContext();
  const { gameState } = useGameState();

  const propertyState: PropertyState = gameState.propertyStates[index];
  const ownerState = propertyState.ownerId ? gameState.playerStates[propertyState.ownerId] : undefined;

  const houseContainerOrientation: FieldOrientation = useMemo(() =>
      position === FieldPosition.left || position === FieldPosition.right
        ? FieldOrientation.horizontal
        : FieldOrientation.vertical,
    [position]);

  const onFieldClick = () => {
    openPopUpModal({
      content: <PropertyManagementModal fieldIndex={index}/>,
      isRounded: true,
    });
  };

  const houseAmount = useMemo(() => {
    return PROPERTY_FIELDS_DATA[index].housePrice && propertyState.houses
      ? propertyState.houses
      : 0;
  }, [index, propertyState.houses]);

  return (
    <div
      className={defineFieldClassname(position)}
      onClick={onFieldClick}
      style={{ cursor: 'zoom-in' }}
    >
      {ownerState && <OwnerCover color={ownerState.color}/>}
      {houseAmount > 0 && houseAmount <= MAX_HOUSES &&
        <HouseContainer
          amount={houseAmount}
          orientation={houseContainerOrientation}
        />
      }
      {propertyState.isMortgaged && <MortgageTag/>}
      <PriceTag priceTag={propertyState.priceTag}/>
    </div>
  )
}

export default PropertyField;
