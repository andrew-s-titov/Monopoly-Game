import { FieldPosition } from '../types/enums';
import { memo } from 'react';
import { defineFieldClassname } from "../utils/property";

interface IFieldProps {
  position: FieldPosition;
}

const ActionField = ({position}: IFieldProps) => {

  return <div className={defineFieldClassname(position)}></div>;
}

export default memo(ActionField);