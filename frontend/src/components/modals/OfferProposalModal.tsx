import { memo } from "react";
import { Button } from "primereact/button";

import { PROPERTY_FIELDS_DATA } from "../../constants";
import { UPropertyIndex } from "../../types/unions";
import PropertyOfferView from "../PropertyOfferView";
import { useEventModalContext } from "../../context/EventModalProvider";
import useQuery from "../../hooks/useQuery";
import { ModalId } from "./index";
import { useGameState } from "../../context/GameStateProvider";
import { useTranslations } from "../../i18n/config";

interface IOfferProposalModalProps {
  initiatorName: string,
  initiatorFields: UPropertyIndex[],
  addresseeFields: UPropertyIndex[],
  initiatorMoney: number,
  addresseeMoney: number,
}

const OfferProposalModal = ({
                              initiatorName,
                              addresseeFields,
                              initiatorFields,
                              addresseeMoney,
                              initiatorMoney
                            }: IOfferProposalModalProps) => {

  const { t } = useTranslations();
  const { gameId } = useGameState();
  const { closeEventModal } = useEventModalContext();
  const closeProposal = () => closeEventModal(ModalId.OFFER_PROPOSAL);
  const { put } = useQuery();
  const { execute: accept, isLoading: isAcceptLoading } = put({
    url: `/game/${gameId}/offer?action=ACCEPT`,
    onSuccess: closeProposal,
  });
  const { execute: decline, isLoading: isDeclineLoading } = put({
    url: `/game/${gameId}/offer?action=DECLINE`,
    onSuccess: closeProposal,
  })

  const initiatorTotal = initiatorFields
    .map(index => PROPERTY_FIELDS_DATA[index].price)
    .reduce((a, b) => a + b, 0) + initiatorMoney;
  const addresseeTotal = addresseeFields
    .map(index => PROPERTY_FIELDS_DATA[index].price)
    .reduce((a, b) => a + b, 0) + addresseeMoney;

  return (
    <div className='offer-content'>
      <div className='offer-sides-container'>
        <div className='offer-side'>
          <span className='offer-side-title'>{t('deal.you')}</span>
          <span>{`$${addresseeMoney}`}</span>
          <div className='offer-checkbox-container'>
            {addresseeFields.map(fieldIndex =>
              <div key={`${fieldIndex}`}>
                <PropertyOfferView
                  name={PROPERTY_FIELDS_DATA[fieldIndex].name}
                  group={PROPERTY_FIELDS_DATA[fieldIndex].group}
                />
              </div>
            )}
          </div>
          <div className='offer-total'>{t('deal.total', { total: addresseeTotal })}</div>
        </div>
        <div className='offer-side'>
          <span className='offer-side-title'>{`${initiatorName}:`}</span>
          <span>{`$${initiatorMoney}`}</span>
          <div className='offer-checkbox-container'>
            {initiatorFields.map(fieldIndex =>
              <div key={`${fieldIndex}`}>
                <PropertyOfferView
                  name={PROPERTY_FIELDS_DATA[fieldIndex].name}
                  group={PROPERTY_FIELDS_DATA[fieldIndex].group}
                />
              </div>
            )}
          </div>
          <div className='offer-total'>{t('deal.total', { total: initiatorTotal })}</div>
        </div>
      </div>
      <div className='modal-button-group'>
        <Button
          loading={isAcceptLoading}
          loadingIcon="pi pi-spin pi-box modal-button-icon"
          className='modal-button'
          label={t('action.accept')}
          severity='success'
          icon='pi pi-check modal-button-icon'
          onClick={() => accept()}
        />
        <Button
          loading={isDeclineLoading}
          loadingIcon="pi pi-spin pi-box modal-button-icon"
          className='modal-button'
          label={t('action.decline')}
          severity='secondary'
          icon='pi pi-times modal-button-icon'
          onClick={() => decline()}
        />
      </div>
    </div>
  );
}

export default memo(OfferProposalModal);
