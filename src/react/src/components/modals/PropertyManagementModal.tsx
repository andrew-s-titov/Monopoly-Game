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
  const { get } = useQuery();
  const { addHousePurchase } = useGameState();

  const { canManage, managementOptions } = getManagement(fieldIndex);
  const {
    showSellHouse,
    showRedeem,
    showBuyHouse,
    showMortgage
  } = managementOptions;

  const callManagementEndpoint = (urlPart: string) => {
    get({
      url: `${BE_ENDPOINT}/game/field/${fieldIndex}/${urlPart}`,
      onSuccess: closePopUpModal,
    });
  }

  const onBuyHouse = () => {
    get({
      url: `${BE_ENDPOINT}/game/field/${fieldIndex}/buy_house`,
      onSuccess: () => {
        addHousePurchase(PROPERTY_FIELDS_DATA[fieldIndex].group);
        closePopUpModal();
      },
    });
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'row' }}>
      <img
        src={fieldIndex && require(`../../assets/images/info${fieldIndex}.png`)}
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
              onClick={() => callManagementEndpoint('redeem')}
            />
          }
          {showMortgage &&
            <Button
              label='Mortgage'
              outlined
              severity="secondary"
              icon="pi pi-file-excel icon"
              className="property-management-button"
              onClick={() => callManagementEndpoint('mortgage')}
            />
          }
          {showBuyHouse &&
            <Button
              label='Buy a house'
              outlined
              severity="secondary"
              icon="pi pi-money-bill icon"
              className="property-management-button"
              onClick={onBuyHouse}
            />
          }
          {showSellHouse &&
            <Button
              label='Sell a house'
              outlined
              severity="secondary"
              icon="pi pi-home icon"
              className="property-management-button"
              onClick={() => callManagementEndpoint('sell_house')}
            />
          }
        </div>
      }
    </div>
  );
}

export default PropertyManagementModal;