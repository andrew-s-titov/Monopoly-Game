import React, { memo } from 'react';
import { PropertyGroup } from "../types/enums";

const propertyIcons: Record<PropertyGroup, React.JSX.Element> = {
  [PropertyGroup.airports]: <i className='pi pi-send field-offer-view-icon'/>,
  [PropertyGroup.companies]: <i className='pi pi-briefcase field-offer-view-icon'/>,
  [PropertyGroup.yellow]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#d6e68a'}}
    />,
  [PropertyGroup.sky]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#8ac7e6'}}
    />,
  [PropertyGroup.purple]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#855c99'}}
    />,
  [PropertyGroup.brown]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#995c5c'}}
    />,
  [PropertyGroup.orange]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#e6a88a'}}
    />,
  [PropertyGroup.mustard]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#e6d68a'}}
    />,
  [PropertyGroup.green]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#4d804d'}}
    />,
  [PropertyGroup.blue]:
    <i
      className='pi pi-circle-fill field-offer-view-icon'
      style={{color: '#4d6f80'}}
    />,
}

interface IPropertyOfferViewProps {
  name: string;
  group: PropertyGroup,
}

const PropertyOfferView = ({name, group}: IPropertyOfferViewProps) => {
  return (
    <div className="offer-field-view">
      <span>{name}</span>
      {propertyIcons[group]}
    </div>
  );
}

export default memo(PropertyOfferView);