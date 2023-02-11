import * as Buttons from './buttons.js';
import * as HttpUtils from './http.js';
import {removeElementsIfPresent} from './utils.js';

const REPLY_WAITING_SCREEN_ID = 'reply-waiting-screen';
const MONEY_TO_GIVE_INPUT_ID = 'money-to-give-input';
const MONEY_TO_RECEIVE_INPUT_ID = 'money-to-receive-input';
const OFFER_INITIATOR_CHECKBOX_GROUP_NAME = 'initiator-checkboxes';
const OFFER_ADDRESSEE_CHECKBOX_GROUP_NAME = 'addressee-checkboxes';

export function startOfferProcess(addresseeId) {
    HttpUtils.get(`${HttpUtils.baseGameUrl()}/offer/${addresseeId}/info`,
        (preDealInfo) => {
            const offerInfoBox = renderOfferInfoBox();
            renderOfferInfoBoxDescription(offerInfoBox,
                'Choose fields to buy or sell and enter money to exchange');

            const initiatorInfoContainer = renderOfferSideContainer(offerInfoBox, 'left', 'You:');
            renderMoneyInput(initiatorInfoContainer, MONEY_TO_GIVE_INPUT_ID, MONEY_TO_RECEIVE_INPUT_ID);
            renderSideFieldCheckboxes(initiatorInfoContainer,
                preDealInfo.offer_initiator_fields, OFFER_INITIATOR_CHECKBOX_GROUP_NAME);

            const addresseeInfoContainer = renderOfferSideContainer(offerInfoBox, 'right', 'Contractor:');
            renderMoneyInput(addresseeInfoContainer, MONEY_TO_RECEIVE_INPUT_ID, MONEY_TO_GIVE_INPUT_ID);
            renderSideFieldCheckboxes(addresseeInfoContainer,
                preDealInfo.offer_addressee_fields, OFFER_ADDRESSEE_CHECKBOX_GROUP_NAME);

            renderLeftButton(offerInfoBox, 'Send an offer', () => {
                HttpUtils.post(`${HttpUtils.baseGameUrl()}/offer/${addresseeId}/send`, createOfferBody(),
                    () => {
                        Buttons.hideThrowDiceButton();
                        Buttons.removeOldActionContainer();
                        offerInfoBox.remove();
                    }
                );
            });
            renderRightButton(offerInfoBox, 'Cancel', () => offerInfoBox.remove());
        });
}

export function renderOfferProposal(offerProposal) {
    const initiatorName = offerProposal.initiator_name;
    const fieldsToBuy = offerProposal.fields_to_buy;
    const fieldsToSell = offerProposal.fields_to_sell;
    const moneyToGive = offerProposal.money_to_give;
    const moneyToReceive = offerProposal.money_to_receive;

    const offerInfoBox = renderOfferInfoBox();
    renderOfferInfoBoxDescription(offerInfoBox, `${initiatorName} made you an offer:`);

    const addresseeInfoContainer = renderOfferSideContainer(offerInfoBox, 'left', 'You give:');
    const addresseeBulletList = document.createElement('ul');
    addresseeInfoContainer.appendChild(addresseeBulletList);
    renderMoneyProposal(addresseeBulletList, moneyToReceive);
    renderFieldsProposal(addresseeBulletList, fieldsToBuy);

    const initiatorInfoContainer = renderOfferSideContainer(offerInfoBox, 'right', `${initiatorName} gives:`);
    const initiatorBulletList = document.createElement('ul');
    initiatorInfoContainer.appendChild(initiatorBulletList);
    renderMoneyProposal(initiatorBulletList, moneyToGive);
    renderFieldsProposal(initiatorBulletList, fieldsToSell);

    renderLeftButton(offerInfoBox, 'Accept', () => {
        HttpUtils.post(`${HttpUtils.baseGameUrl()}/offer/process?action=ACCEPT`);
        offerInfoBox.remove();
    });
    renderRightButton(offerInfoBox, 'Decline', () => {
        HttpUtils.post(`${HttpUtils.baseGameUrl()}/offer/process?action=DECLINE`);
        offerInfoBox.remove();
    });
}

function createOfferBody() {
    const fieldsToSell = getCheckedFieldValues(OFFER_INITIATOR_CHECKBOX_GROUP_NAME);
    const fieldsToBuy = getCheckedFieldValues(OFFER_ADDRESSEE_CHECKBOX_GROUP_NAME);
    const moneyToGive = document.getElementById(MONEY_TO_GIVE_INPUT_ID).value;
    const moneyToReceive = document.getElementById(MONEY_TO_RECEIVE_INPUT_ID).value;
    return new Offer(fieldsToSell, fieldsToBuy, moneyToGive, moneyToReceive);
}

export function renderReplyWaitingScreen() {
    const replyWaitingScreen = document.createElement('div');
    replyWaitingScreen.className = 'offer-reply-waiting-screen';
    replyWaitingScreen.id = REPLY_WAITING_SCREEN_ID;
    document.getElementById('message-container').appendChild(replyWaitingScreen);

    const waitingMessage = document.createElement('p');
    waitingMessage.innerText = 'Waiting for reply...';
    replyWaitingScreen.appendChild(waitingMessage);
}

export function removeReplyWaitingScreen() {
    removeElementsIfPresent(REPLY_WAITING_SCREEN_ID);
}

function renderOfferInfoBox() {
    const offerInfoBoxId = 'offer-info-box';
    removeElementsIfPresent('offer-info-box');
    const offerInfoBox = document.createElement('div');
    offerInfoBox.id = offerInfoBoxId;
    offerInfoBox.className = 'offer-info-box';
    document.getElementById('message-container').appendChild(offerInfoBox);
    return offerInfoBox;
}

function renderOfferInfoBoxDescription(offerInfoBox, description) {
    const text = document.createElement('span');
    text.innerText = description;
    text.className = 'offer-box-description';
    offerInfoBox.appendChild(text);
}

function renderOfferSideContainer(offerInfoBox, side, description) {
    const offerSideContainer = document.createElement('div');
    offerSideContainer.className = 'offer-side-info-container';
    offerSideContainer.style.float = side;

    const initiatorFieldText = document.createElement('p');
    initiatorFieldText.innerText = description;
    initiatorFieldText.className = 'offer-side-name';
    offerSideContainer.appendChild(initiatorFieldText);

    offerInfoBox.appendChild(offerSideContainer);
    return offerSideContainer;
}

function renderLeftButton(offerInfoBox, name, clickFunction) {
    const leftButton = renderBottomButton(offerInfoBox, name, clickFunction);
    leftButton.style.left = '20%';
}

function renderRightButton(offerInfoBox, name, clickFunction) {
    const leftButton = renderBottomButton(offerInfoBox, name, clickFunction);
    leftButton.style.right = '20%';
}

function renderBottomButton(offerInfoBox, name, clickFunction) {
    const button = document.createElement('button');
    button.className = 'offer-button';
    button.innerText = name;
    Buttons.addClickEvent(button, clickFunction);
    offerInfoBox.appendChild(button);
    return button;
}

function renderMoneyInput(sideInfoContainer, id, mutuallyExclusiveInputId) {
    const moneyInput = document.createElement('input');
    moneyInput.id = id;
    moneyInput.placeholder = 'sum of money...';
    moneyInput.className = 'offer-money-input';
    moneyInput.oninput = () => {
        moneyInput.value = moneyInput.value.replace(/\D/g, '').replace(/^0[^.]/, '0');
        if (moneyInput.value !== '' && moneyInput.value !== '0') {
            const anotherMoneyInput = document.getElementById(mutuallyExclusiveInputId);
            if (anotherMoneyInput) {
                anotherMoneyInput.value = '';
            }
        }
    }
    sideInfoContainer.appendChild(moneyInput);
}

function renderSideFieldCheckboxes(sideInfoContainer, fields, groupName) {
    const fieldCheckboxList = document.createElement('div');
    fieldCheckboxList.className = 'offer-fields-checkbox-container';
    for (let field of fields) {
        const fieldInfoCheckbox = document.createElement('input');
        fieldInfoCheckbox.type = 'checkbox';
        fieldInfoCheckbox.name = groupName;
        fieldInfoCheckbox.value = field.id;
        fieldInfoCheckbox.id = `${field.id}-checkbox`;

        const checkboxLabel = document.createElement('label');
        checkboxLabel.for = fieldInfoCheckbox.id;
        checkboxLabel.innerText = field.name;

        const checkboxLine = document.createElement('div');
        checkboxLine.append(fieldInfoCheckbox, checkboxLabel);
        fieldCheckboxList.appendChild(checkboxLine);
    }
    sideInfoContainer.appendChild(fieldCheckboxList);
}

function renderMoneyProposal(sideInfoContainer, moneyAmount) {
    if (moneyAmount && moneyAmount > 0) {
        const moneyProposal = document.createElement('li');
        moneyProposal.innerText = `money: $${moneyAmount}`;
        sideInfoContainer.appendChild(moneyProposal);
    }
}

function renderFieldsProposal(sideInfoContainer, fields) {
    if (fields && fields.length > 0) {
        for (let field of fields) {
            const fieldName = document.createElement('li');
            fieldName.innerText = field.name;
            sideInfoContainer.appendChild(fieldName);
        }
    }
}

function getCheckedFieldValues(groupName) {
    const checkedFields = [...document.querySelectorAll(`input[name=${groupName}]:checked`)];
    let fieldsValues;
    if (checkedFields && checkedFields.length > 0) {
        fieldsValues = checkedFields.map(checkbox => checkbox.value);
    }
    return fieldsValues;
}

class Offer {
    fields_to_sell;
    fields_to_buy;
    money_to_give;
    money_to_receive;

    constructor(fieldsToSell, fieldsToBuy, moneyToGive, moneyToReceive) {
        this.fields_to_sell = fieldsToSell;
        this.fields_to_buy = fieldsToBuy;
        this.money_to_give = moneyToGive;
        this.money_to_receive = moneyToReceive;
    }
}