import { Dispatch, memo, SetStateAction, useMemo, useState } from 'react';

import { Checkbox, CheckboxChangeEvent } from 'primereact/checkbox';
import { Button } from 'primereact/button';
import { InputNumber } from 'primereact/inputnumber';

import { useGameState } from '../../context/GameStateProvider';
import { Deal, PropertyShortInfo, PropertyState } from '../../types/interfaces';
import { PROPERTY_FIELDS_DATA } from '../../constants';
import { getEntries } from '../../utils/global';
import { UPropertyIndex } from '../../types/unions';
import { useMessageContext } from '../../context/MessageProvider';
import PropertyOfferView from '../PropertyOfferView';
import useQuery from "../../hooks/useQuery";
import { usePopUpModalContext } from "../../context/PopUpModalProvider";
import { BE_ENDPOINT } from "../../api/config";
import { useEventModalContext } from "../../context/EventModalProvider";

interface IOfferDealModalProps {
  addresseeId: string,
}

const extractPlayerFields = (propertyStates: Record<UPropertyIndex, PropertyState>, playerId: string)
  : PropertyShortInfo[] =>
  getEntries(propertyStates)
    .filter(([_, propertyState]) => playerId === propertyState.ownerId)
    .filter(([_, propertyState]) => !propertyState.houses)
    .map(([fieldIndex]) => {
      return {
        fieldIndex,
        name: PROPERTY_FIELDS_DATA[fieldIndex].name,
        group: PROPERTY_FIELDS_DATA[fieldIndex].group,
      }
    });

const onCheckedFieldsChange = (event: CheckboxChangeEvent,
                               useStateReturn: [UPropertyIndex[], Dispatch<SetStateAction<UPropertyIndex[]>>]) => {
  const [state, setState] = useStateReturn;
  let checkedFields = [...state];

  event.checked
    ? checkedFields.push(event.value)
    : checkedFields.splice(checkedFields.indexOf(event.value), 1);

  setState(checkedFields);
}

const onOfferMoneyChange = (newValue: number | undefined | null,
                            setState: Dispatch<SetStateAction<number>>,
                            maxValue: number) => {
  if (!newValue) {
    setState(0);
  } else if (newValue >= 0) {
    newValue <= maxValue
      ? setState(newValue)
      : setState(maxValue)
  }
}

const OfferDealModal = ({ addresseeId }: IOfferDealModalProps) => {

  const { post, isLoading } = useQuery();
  const checkedInitiatorFieldsUseStateReturn = useState<UPropertyIndex[]>([]);
  const checkedInitiatorFields = checkedInitiatorFieldsUseStateReturn[0];
  const checkedAddresseeFieldsUseStateReturn = useState<UPropertyIndex[]>([]);
  const checkedAddresseeFields = checkedAddresseeFieldsUseStateReturn[0];
  const [selectedInitiatorMoney, setSelectedInitiatorMoney] = useState(0);
  const [selectedAddresseeMoney, setSelectedAddresseeMoney] = useState(0);
  const { showWarning } = useMessageContext();

  const { closePopUpModal } = usePopUpModalContext();
  const { closeEventModal } = useEventModalContext();
  const { gameState } = useGameState();
  const addresseeState = gameState.playerStates[addresseeId];
  const initiatorId = gameState.currentUserId;
  const initiatorState = gameState.playerStates[initiatorId];

  const initiatorFields = useMemo(
    () => extractPlayerFields(gameState.propertyStates, initiatorId),
    [gameState.propertyStates]
  );
  const addresseeFields = useMemo(
    () => extractPlayerFields(gameState.propertyStates, addresseeId),
    [gameState.propertyStates]);

  const initiatorTotal = checkedInitiatorFields
    .map(index => PROPERTY_FIELDS_DATA[index].price)
    .reduce((a, b) => a + b, 0) + selectedInitiatorMoney;
  const addresseeTotal = checkedAddresseeFields
    .map(index => PROPERTY_FIELDS_DATA[index].price)
    .reduce((a, b) => a + b, 0) + selectedAddresseeMoney;

  const closeAllModals = () => {
    closePopUpModal();
    closeEventModal();
  }

  const onSendOffer = () => {
    if (!selectedInitiatorMoney
      && !selectedAddresseeMoney
      && !checkedInitiatorFields.length
      && !checkedAddresseeFields.length) {
      showWarning('Cannot send empty offer');
      return;
    }
    const offer: Deal = {
      initiatorMoney: selectedInitiatorMoney,
      initiatorFields: checkedInitiatorFields,
      addresseeMoney: selectedAddresseeMoney,
      addresseeFields: checkedAddresseeFields,
    }
    post({
      url: `${BE_ENDPOINT}/game/offer/${addresseeId}/send`,
      body: offer,
      onSuccess: closeAllModals,
    })
  }

  return (
    <div className='offer-content'>
      <div className='offer-sides-container'>
        <div className='offer-side'>
          <span className='offer-side-title'>You:</span>
          <InputNumber
            className='offer-money'
            prefix='$'
            min={0}
            max={initiatorState.money}
            maxLength={5}
            value={selectedInitiatorMoney}
            placeholder='money amount'
            onChange={event => onOfferMoneyChange(event.value, setSelectedInitiatorMoney, initiatorState.money)}
            inputClassName='offer-money-input'
          />
          <div
            className='offer-checkbox-container'
          >
            {initiatorFields.map(field =>
              <div key={`${field.fieldIndex}-checkbox`}>
                <Checkbox
                  className='offer-checkbox'
                  inputId={field.fieldIndex.toString()}
                  value={field.fieldIndex}
                  checked={checkedInitiatorFields.includes(field.fieldIndex)}
                  onChange={event => onCheckedFieldsChange(event, checkedInitiatorFieldsUseStateReturn)}
                />
                <label
                  htmlFor={field.fieldIndex.toString()}
                >
                  <PropertyOfferView
                    name={field.name}
                    group={field.group}
                  />
                </label>
              </div>
            )}
          </div>
          <div className='offer-total'>Total: ${initiatorTotal}</div>
        </div>
        <div className='offer-side'>
          <span className='offer-side-title'>{`${addresseeState ? addresseeState.name : ''}:`}</span>
          <InputNumber
            className='offer-money'
            prefix='$'
            min={0}
            max={addresseeState.money}
            maxLength={5}
            value={selectedAddresseeMoney}
            placeholder='money amount'
            onChange={event => onOfferMoneyChange(event.value, setSelectedAddresseeMoney, addresseeState.money)}
            inputClassName='offer-money-input'
          />
          <div
            className='offer-checkbox-container'
          >
            {addresseeFields.map(field =>
              <div key={`${field.fieldIndex}-checkbox`}>
                <Checkbox
                  className='offer-checkbox'
                  inputId={field.fieldIndex.toString()}
                  value={field.fieldIndex}
                  checked={checkedAddresseeFields.includes(field.fieldIndex)}
                  onChange={event => onCheckedFieldsChange(event, checkedAddresseeFieldsUseStateReturn)}
                />
                <label
                  htmlFor={field.fieldIndex.toString()}
                >
                  <PropertyOfferView
                    name={field.name}
                    group={field.group}
                  />
                </label>
              </div>
            )}
          </div>
          <div className='offer-total'>Total: ${addresseeTotal}</div>
        </div>
      </div>
      <div className='modal-button-group'>
        <Button
          className='modal-button'
          label='Cancel'
          severity='secondary'
          icon='pi pi-times modal-button-icon'
          onClick={closePopUpModal}
        />
        <Button
          loading={isLoading}
          loadingIcon="pi pi-spin pi-box modal-button-icon"
          className='modal-button'
          label='Send offer'
          severity='success'
          icon='pi pi-file-edit modal-button-icon'
          onClick={onSendOffer}
        />
      </div>
    </div>
  );
}

export default memo(OfferDealModal);