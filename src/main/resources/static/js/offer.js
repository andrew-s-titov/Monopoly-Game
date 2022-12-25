import {addClickEvent} from "./buttons.js";
import {getBaseGameUrl, sendPostHttpRequest} from "./http.js";

export function createOffer() {
    let initiatorFields = [...document.querySelectorAll('input[name=initiator-checkboxes]:checked')];
    let fieldsToSell;
    if (initiatorFields && initiatorFields.length > 0) {
        fieldsToSell = initiatorFields.map(checkbox => checkbox.value);
    }
    let addresseeFields = [...document.querySelectorAll('input[name=addressee-checkboxes]:checked')];
    let fieldsToBuy;
    if (addresseeFields && addresseeFields.length > 0) {
        fieldsToBuy = addresseeFields.map(checkbox => checkbox.value);
    }
    let moneyToGive = document.getElementById('money-to-give-input').value;
    let moneyToReceive = document.getElementById('money-to-receive-input').value;
    return new Offer(fieldsToSell, fieldsToBuy, moneyToGive, moneyToReceive);
}

export function renderOfferProposal(offerProposal) {
    let initiatorName = offerProposal.initiator_name;
    let fieldsToBuy = offerProposal.fields_to_buy;
    let fieldsToSell = offerProposal.fields_to_sell;
    let moneyToGive = offerProposal.money_to_give;
    let moneyToReceive = offerProposal.money_to_receive;

    let offerInfoBox = document.createElement('div');
    offerInfoBox.className = 'offer-info-box';

    let text = document.createElement('span');
    text.innerText = `${initiatorName} made you an offer:`;
    text.className = 'offer-box-description';
    offerInfoBox.appendChild(text);

    let addresseeInfoContainer = document.createElement('div');
    addresseeInfoContainer.className = 'offer-side-info-container';
    addresseeInfoContainer.style.float = 'left';

    let addresseeFieldText = document.createElement('p');
    addresseeFieldText.innerText = 'You give:';
    addresseeFieldText.className = 'offer-side-name';
    addresseeInfoContainer.appendChild(addresseeFieldText);

    if (moneyToReceive && moneyToReceive > 0) {
        let moneyAddresseeGives = document.createElement('p');
        moneyAddresseeGives.innerText = `money: $${moneyToReceive}`;
        addresseeInfoContainer.appendChild(moneyAddresseeGives);
        addresseeInfoContainer.appendChild(document.createElement('br'));
    }

    if (fieldsToBuy && fieldsToBuy.length > 0) {
        for (let field of fieldsToBuy) {
            let fieldName = document.createElement('p');
            fieldName.innerText = field.name;
            addresseeInfoContainer.appendChild(fieldName);
            addresseeInfoContainer.appendChild(document.createElement('br'));
        }
    }
    offerInfoBox.appendChild(addresseeInfoContainer);

    let initiatorInfoContainer = document.createElement('div');
    initiatorInfoContainer.className = 'offer-side-info-container';
    initiatorInfoContainer.style.float = 'right';

    let initiatorFieldText = document.createElement('span');
    initiatorFieldText.innerText = `${initiatorName} gives:`;
    initiatorFieldText.className = 'offer-side-name';
    initiatorInfoContainer.appendChild(initiatorFieldText);

    if (moneyToGive && moneyToGive > 0) {
        let moneyInitiatorGives = document.createElement('p');
        moneyInitiatorGives.innerText = `money: $${moneyToGive}`;
        initiatorInfoContainer.appendChild(moneyInitiatorGives);
        initiatorInfoContainer.appendChild(document.createElement('br'));
    }

    if (fieldsToSell && fieldsToSell.length > 0) {
        for (let field of fieldsToSell) {
            let fieldName = document.createElement('p');
            fieldName.innerText = field.name;
            initiatorInfoContainer.appendChild(fieldName);
            initiatorInfoContainer.appendChild(document.createElement('br'));
        }
    }
    offerInfoBox.appendChild(initiatorInfoContainer);

    let acceptButton = document.createElement('button');
    acceptButton.className = 'offer-button';
    acceptButton.style.left = '20%';
    acceptButton.innerText = 'Accept';
    addClickEvent(acceptButton, () => {
        sendPostHttpRequest(`${getBaseGameUrl()}/offer/process?action=ACCEPT`, true);
        offerInfoBox.remove();
    });
    offerInfoBox.appendChild(acceptButton);

    let declineButton = document.createElement('button');
    declineButton.className = 'offer-button';
    declineButton.style.right = '20%';
    declineButton.innerText = 'Decline';
    addClickEvent(declineButton, () => {
        sendPostHttpRequest(`${getBaseGameUrl()}/offer/process?action=DECLINE`, true);
        offerInfoBox.remove();
    });
    offerInfoBox.appendChild(declineButton);

    document.getElementById('message-container').appendChild(offerInfoBox);
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