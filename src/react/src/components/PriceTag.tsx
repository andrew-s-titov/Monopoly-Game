import {memo} from 'react';

interface IPriceTagProps {
  priceTag: string;
}

const PriceTag = ({ priceTag }: IPriceTagProps) => {
  return (
    <div className='price-tag'>
      {priceTag}
    </div>
  );
}

export default memo(PriceTag);
