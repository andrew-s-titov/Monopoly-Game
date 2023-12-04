import {memo} from 'react';
import {FieldOrientation} from "../types/enums";

interface IHouseContainerProps {
  amount: number;
  orientation: FieldOrientation;
}

const HouseContainer = ({amount}: IHouseContainerProps) => {

  if (amount < 1 || amount > 5) {
    return null;
  }

  return (
    <div className='house-container'>
      {amount < 5
        ? Array.from({length: amount}, (_, index) => <i key={index} className='pi pi-home house-pic'/>)
        : <i className='pi pi-building hotel-pic'/>}
    </div>
  );
}

export default memo(HouseContainer);
