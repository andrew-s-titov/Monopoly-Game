import PropertyField from "./PropertyField";
import ActionField from "./ActionField";
import { FieldPosition } from "../types/enums";
import Chat from "./chat/Chat";
import PlayerChipsContainer from "./PlayerChipsContainer";

const GameMap = () => {

  return (
    <div className='map-container'>
      <div className='map'>
        <PlayerChipsContainer />
        <div className='map-fields-row'>
          <ActionField position={FieldPosition.corner}/>
          <PropertyField position={FieldPosition.top} index={1}/>
          <ActionField position={FieldPosition.top}/>
          <PropertyField position={FieldPosition.top} index={3}/>
          <ActionField position={FieldPosition.top}/>
          <PropertyField position={FieldPosition.top} index={5}/>
          <PropertyField position={FieldPosition.top} index={6}/>
          <ActionField position={FieldPosition.top}/>
          <PropertyField position={FieldPosition.top} index={8}/>
          <PropertyField position={FieldPosition.top} index={9}/>
          <ActionField position={FieldPosition.top}/>
        </div>
        <div className='map-center-row'>
          <div className='map-fields-column'>
            <PropertyField position={FieldPosition.left} index={39}/>
            <ActionField position={FieldPosition.left}/>
            <PropertyField position={FieldPosition.left} index={37}/>
            <ActionField position={FieldPosition.left}/>
            <PropertyField position={FieldPosition.left} index={35}/>
            <PropertyField position={FieldPosition.left} index={34}/>
            <ActionField position={FieldPosition.left}/>
            <PropertyField position={FieldPosition.left} index={32}/>
            <PropertyField position={FieldPosition.left} index={31}/>
          </div>
          <div className='map-center'>
            <Chat />
          </div>
          <div className='map-fields-column'>
            <PropertyField position={FieldPosition.right} index={11}/>
            <PropertyField position={FieldPosition.right} index={12}/>
            <PropertyField position={FieldPosition.right} index={13}/>
            <PropertyField position={FieldPosition.right} index={14}/>
            <PropertyField position={FieldPosition.right} index={15}/>
            <PropertyField position={FieldPosition.right} index={16}/>
            <ActionField position={FieldPosition.right}/>
            <PropertyField position={FieldPosition.right} index={18}/>
            <PropertyField position={FieldPosition.right} index={19}/>
          </div>
        </div>
        <div className='map-fields-row'>
          <ActionField position={FieldPosition.corner}/>
          <PropertyField position={FieldPosition.bottom} index={29}/>
          <PropertyField position={FieldPosition.bottom} index={28}/>
          <PropertyField position={FieldPosition.bottom} index={27}/>
          <PropertyField position={FieldPosition.bottom} index={26}/>
          <PropertyField position={FieldPosition.bottom} index={25}/>
          <PropertyField position={FieldPosition.bottom} index={24}/>
          <PropertyField position={FieldPosition.bottom} index={23}/>
          <ActionField position={FieldPosition.bottom}/>
          <PropertyField position={FieldPosition.bottom} index={21}/>
          <ActionField position={FieldPosition.corner}/>
        </div>
      </div>
    </div>
  );
}

export default GameMap;
