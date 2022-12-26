import {addClickEvent} from "./buttons.js";
import {getBaseGameUrl, sendGetHttpRequest, sendPostHttpRequest} from "./http.js";

const REPLY_WAITING_SCREEN_ID = 'reply-waiting-screen';

export function startOfferProcess(addresseeId) {
    sendGetHttpRequest(`${getBaseGameUrl()}/offer/${addresseeId}/info`, true,
        function (requester) {
            if (requester.readyState === XMLHttpRequest.DONE && requester.status === 200) {
                let preDealInfo = JSON.parse(requester.response);

                let offerInfoBox = renderOfferInfoBox();
                renderOfferInfoBoxDescription(offerInfoBox,
                    'Choose fields to buy or sell and enter money to exchange');
                let initiatorInfoContainer = renderOfferSideContainer(offerInfoBox, 'left', 'You:');

                let moneyToGiveInput = document.createElement('input');
                moneyToGiveInput.id = 'money-to-give-input';
                moneyToGiveInput.placeholder = 'sum of money...';
                moneyToGiveInput.style.fontSize = '15px';
                moneyToGiveInput.oninput = () => {
                    moneyToGiveInput.value = moneyToGiveInput.value
                        .replace(/\D/g, '')
                        .replace(/^0[^.]/, '0');
                    if (moneyToGiveInput.value !== '' && moneyToGiveInput.value !== '0') {
                        document.getElementById('money-to-receive-input').value = '';
                    }
                }
                initiatorInfoContainer.appendChild(moneyToGiveInput);

                let initiatorFieldList = document.createElement('div');
                let initiatorCheckboxName = 'initiator-checkboxes';
                for (let field of preDealInfo.offer_initiator_fields) {
                    let fieldInfoCheckbox = document.createElement('input');
                    fieldInfoCheckbox.type = 'checkbox';
                    fieldInfoCheckbox.name = initiatorCheckboxName;
                    fieldInfoCheckbox.value = field.id;
                    fieldInfoCheckbox.id = `${field.id}-checkbox`;

                    let checkboxLabel = document.createElement('label');
                    checkboxLabel.for = fieldInfoCheckbox.id;
                    checkboxLabel.innerText = field.name;

                    initiatorFieldList.appendChild(fieldInfoCheckbox);
                    initiatorFieldList.appendChild(checkboxLabel);
                    initiatorFieldList.appendChild(document.createElement('br'));
                }
                initiatorInfoContainer.appendChild(initiatorFieldList);

                let addresseeInfoContainer = renderOfferSideContainer(offerInfoBox, 'right', 'Contractor:');

                let moneyToReceiveInput = document.createElement('input');
                moneyToReceiveInput.id = 'money-to-receive-input';
                moneyToReceiveInput.style.fontSize = '15px';
                moneyToReceiveInput.placeholder = 'sum of money...';
                moneyToReceiveInput.oninput = () => {
                    moneyToReceiveInput.value = moneyToReceiveInput.value
                        .replace(/\D/g, '')
                        .replace(/^0[^.]/, '0');
                    if (moneyToReceiveInput.value !== '' && moneyToReceiveInput.value !== '0') {
                        document.getElementById('money-to-give-input').value = '';
                    }
                }
                addresseeInfoContainer.appendChild(moneyToReceiveInput);

                let addresseeFieldList = document.createElement('div');
                let addresseeCheckboxName = 'addressee-checkboxes';
                for (let field of preDealInfo.offer_addressee_fields) {
                    let fieldInfoCheckbox = document.createElement('input');
                    fieldInfoCheckbox.type = 'checkbox';
                    fieldInfoCheckbox.name = addresseeCheckboxName;
                    fieldInfoCheckbox.value = field.id;
                    fieldInfoCheckbox.id = `${field.id}-checkbox`;

                    let checkboxLabel = document.createElement('label');
                    checkboxLabel.for = fieldInfoCheckbox.id;
                    checkboxLabel.innerText = field.name;

                    addresseeFieldList.appendChild(fieldInfoCheckbox);
                    addresseeFieldList.appendChild(checkboxLabel);
                    addresseeFieldList.appendChild(document.createElement('br'));
                }
                addresseeInfoContainer.appendChild(addresseeFieldList);

                renderLeftButton(offerInfoBox, 'Send an offer', () => {
                    sendPostHttpRequest(
                        `${getBaseGameUrl()}/offer/${addresseeId}/send`, true,
                        function (requester) {
                            if (requester.readyState === XMLHttpRequest.DONE && requester.status === 200) {
                                renderReplyWaitingScreen();
                                offerInfoBox.remove();
                            }
                        },
                        function (requester) {
                            console.error(requester.response)
                            // TODO: show info from server error if user mistake
                        },
                        createOffer()
                    )
                });

                renderRightButton(offerInfoBox, 'Cancel', () => offerInfoBox.remove());
            } else {
                console.error('failed to load available management actions');
                console.log(requester.response);
            }
        }
    );
}

export function renderOfferProposal(offerProposal) {
    let initiatorName = offerProposal.initiator_name;
    let fieldsToBuy = offerProposal.fields_to_buy;
    let fieldsToSell = offerProposal.fields_to_sell;
    let moneyToGive = offerProposal.money_to_give;
    let moneyToReceive = offerProposal.money_to_receive;

    let offerInfoBox = renderOfferInfoBox();
    renderOfferInfoBoxDescription(offerInfoBox, `${initiatorName} made you an offer:`);

    let addresseeInfoContainer = renderOfferSideContainer(offerInfoBox, 'left', 'You give:');
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

    let initiatorInfoContainer = renderOfferSideContainer(offerInfoBox, 'right', `${initiatorName} gives:`);
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

    renderLeftButton(offerInfoBox, 'Accept', () => {
        sendPostHttpRequest(`${getBaseGameUrl()}/offer/process?action=ACCEPT`, true);
        offerInfoBox.remove();
    });
    renderRightButton(offerInfoBox, 'Decline', () => {
        sendPostHttpRequest(`${getBaseGameUrl()}/offer/process?action=DECLINE`, true);
        offerInfoBox.remove();
    });
}

export function removeReplyWaitingScreen() {
    let replyWaitingScreen = document.getElementById(REPLY_WAITING_SCREEN_ID);
    if (replyWaitingScreen) {
        replyWaitingScreen.remove();
    }
}

function createOffer() {
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

function renderReplyWaitingScreen() {
    let offerInfoBox = renderOfferInfoBox();
    offerInfoBox.id = REPLY_WAITING_SCREEN_ID;

    let waitingMessage = document.createElement('p');
    waitingMessage.innerText = 'Waiting for reply...';
    offerInfoBox.appendChild(waitingMessage);
}

function renderOfferInfoBox() {
    let offerInfoBox = document.createElement('div');
    offerInfoBox.className = 'offer-info-box';
    document.getElementById('message-container').appendChild(offerInfoBox);
    return offerInfoBox;
}

function renderOfferInfoBoxDescription(offerInfoBox, description) {
    let text = document.createElement('span');
    text.innerText = description;
    text.className = 'offer-box-description';
    offerInfoBox.appendChild(text);
}

function renderOfferSideContainer(offerInfoBox, side, description) {
    let offerSideContainer = document.createElement('div');
    offerSideContainer.className = 'offer-side-info-container';
    offerSideContainer.style.float = side;

    let initiatorFieldText = document.createElement('p');
    initiatorFieldText.innerText = description;
    initiatorFieldText.className = 'offer-side-name';
    offerSideContainer.appendChild(initiatorFieldText);

    offerInfoBox.appendChild(offerSideContainer);
    return offerSideContainer;
}

function renderLeftButton(offerInfoBox, name, clickFunction) {
    let leftButton = renderBottomButton(offerInfoBox, name, clickFunction);
    leftButton.style.left = '20%';
}

function renderRightButton(offerInfoBox, name, clickFunction) {
    let leftButton = renderBottomButton(offerInfoBox, name, clickFunction);
    leftButton.style.right = '20%';
}

function renderBottomButton(offerInfoBox, name, clickFunction) {
    let button = document.createElement('button');
    button.className = 'offer-button';
    button.innerText = name;
    addClickEvent(button, clickFunction);
    offerInfoBox.appendChild(button);
    return button;
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