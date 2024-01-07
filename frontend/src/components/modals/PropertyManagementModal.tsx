import { UPropertyIndex } from "../../types/unions";
import usePropertyActions from "../../hooks/usePropertyAction";
import useQuery from "../../hooks/useQuery";
import { usePopUpModalContext } from "../../context/PopUpModalProvider";
import { Button } from "primereact/button";
import { useGameState } from "../../context/GameStateProvider";
import { PROPERTY_FIELDS_DATA } from "../../constants";
import { BE_ENDPOINT } from "../../api/config";

interface IPropertyManagementProps {
  fieldIndex: UPropertyIndex;
}

const PropertyManagementModal = ({ fieldIndex }: IPropertyManagementProps) => {

  const { closePopUpModal } = usePopUpModalContext();
  const { getManagement } = usePropertyActions();
  const { put } = useQuery();
  const { addHousePurchase } = useGameState();

  const { canManage, managementOptions } = getManagement(fieldIndex);
  const {
    showSellHouse,
    showRedeem,
    showBuyHouse,
    showMortgage
  } = managementOptions;

  const { execute: redeem } = put({
    url: `${BE_ENDPOINT}/game/field/${fieldIndex}/redeem`,
    onSuccess: closePopUpModal,
  });
  const { execute: sellHouse } = put({
    url: `${BE_ENDPOINT}/game/field/${fieldIndex}/sell_house`,
    onSuccess: closePopUpModal,
  });
  const { execute: mortgage } = put({
    url: `${BE_ENDPOINT}/game/field/${fieldIndex}/mortgage`,
    onSuccess: closePopUpModal,
  });
  const { execute: onBuyHouse } = put({
    url: `${BE_ENDPOINT}/game/field/${fieldIndex}/buy_house`,
    onSuccess: () => {
      addHousePurchase(PROPERTY_FIELDS_DATA[fieldIndex].group);
      closePopUpModal();
    },
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'row' }}>
      <img
        src={fieldIndex && require(`../../assets/images/fields/info${fieldIndex}.png`)}
        alt='property'
        style={{ maxHeight: '70vh', height: '70vh' }}
        draggable='false'
      />
      {canManage
        && <div className="property-management-buttons-container">
          {showRedeem &&
            <Button
              label='Redeem'
              outlined
              severity="secondary"
              icon="pi pi-refresh icon"
              className="property-management-button"
              onClick={() => redeem}
            />
          }
          {showMortgage &&
            <Button
              label='Mortgage'
              outlined
              severity="danger"
              icon="pi pi-file-excel icon"
              className="property-management-button"
              onClick={() => mortgage}
            />
          }
          {showBuyHouse &&
            <Button
              label='Buy a house'
              outlined
              severity="success"
              icon="pi pi-home icon"
              className="property-management-button"
              onClick={() => onBuyHouse}
            />
          }
          {showSellHouse &&
            <Button
              label='Sell a house'
              outlined
              severity="warning"
              icon="pi pi-money-bill icon"
              className="property-management-button"
              onClick={() => sellHouse}
            />
          }
        </div>
      }
    </div>
  );
}

export default PropertyManagementModal;