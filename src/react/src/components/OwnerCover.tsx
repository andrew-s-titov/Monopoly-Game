import {memo} from 'react';

interface IOwnerCover {
  color: string;
}

const OwnerCover = ({color}: IOwnerCover) => {

  return (
    <div
      className='owner-cover'
      style={{
        boxShadow: `inset 0 0 0 0.5vh ${color}`,
      }}>
    </div>
  );

}

export default memo(OwnerCover);