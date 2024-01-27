import { UPropertyIndex } from "../../types/unions";
import usePropertyActions from "../../hooks/usePropertyAction";
import useQuery from "../../hooks/useQuery";
import { usePopUpModalContext } from "../../context/PopUpModalProvider";
import { Button } from "primereact/button";
import { useGameState } from "../../context/GameStateProvider";
import { PROPERTY_FIELDS_DATA } from "../../constants";

interface IPropertyManagementProps {
  fieldIndex: UPropertyIndex;
}

const PropertyManagementModal = ({ fieldIndex }: IPropertyManagementProps) => {

  const { gameId } = useGameState();
  const { closePopUpModal } = usePopUpModalContext();
  const { put } = useQuery();
  const { addHousePurchase } = useGameState();
  const { canManage, availableActions } = usePropertyActions(fieldIndex);
  const {
    showSellHouse,
    showRedeem,
    showBuyHouse,
    showMortgage
  } = availableActions;

  const baseFieldManagementUrl = `/game/${gameId}/field/${fieldIndex}`;

  const { execute: redeem, isLoading: isRedeemLoading } = put({
    url: `${baseFieldManagementUrl}/redeem`,
    onSuccess: closePopUpModal,
  });
  const { execute: sellHouse, isLoading: isSellLoading } = put({
    url: `${baseFieldManagementUrl}/sell_house`,
    onSuccess: closePopUpModal,
  });
  const { execute: mortgage, isLoading: isMortgageLoading } = put({
    url: `${baseFieldManagementUrl}/mortgage`,
    onSuccess: closePopUpModal,
  });
  const { execute: onBuyHouse, isLoading: isBuyLoading } = put({
    url: `${baseFieldManagementUrl}/buy_house`,
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
              loading={isRedeemLoading}
              loadingIcon="pi pi-spin pi-box icon"
              onClick={() => redeem()}
            />
          }
          {showBuyHouse &&
            <Button
              label='Buy a house'
              outlined
              severity="success"
              icon="pi pi-home icon"
              className="property-management-button"
              loading={isBuyLoading}
              loadingIcon="pi pi-spin pi-box icon"
              onClick={() => onBuyHouse()}
            />
          }
          {showSellHouse &&
            <Button
              label='Sell a house'
              outlined
              severity="warning"
              icon="pi pi-money-bill icon"
              className="property-management-button"
              loading={isSellLoading}
              loadingIcon="pi pi-spin pi-box icon"
              onClick={() => sellHouse()}
            />
          }
          {showMortgage &&
            <Button
              label='Mortgage'
              outlined
              severity="danger"
              icon="pi pi-file-excel icon"
              className="property-management-button"
              loading={isMortgageLoading}
              loadingIcon="pi pi-spin pi-box icon"
              onClick={() => mortgage()}
            />
          }
        </div>
      }
    </div>
  );
}

export default PropertyManagementModal;