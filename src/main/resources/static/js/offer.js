import * as Buttons from './buttons.js';
import * as HttpUtils from './http.js';
import * as GameMap from './game-map.js';

const OFFER_INITIATOR_CHECKBOX_GROUP_NAME = 'initiator-checkboxes';
const OFFER_ADDRESSEE_CHECKBOX_GROUP_NAME = 'addressee-checkboxes';

let _OFFER_INFO_BOX = null;
let _OFFER_INFO_BOX_DESCRIPTION = null;

let _LEFT_SIDE_CONTAINER = null;
let _LEFT_MONEY_INPUT = null;
let _LEFT_CHECKBOXES = null;
let _LEFT_OFFER_BUTTON = null;

let _RIGHT_SIDE_CONTAINER = null;
let _RIGHT_CONTAINER_NAME_HOLDER = null;
let _RIGHT_MONEY_INPUT = null;
let _RIGHT_CHECKBOXES = null;
let _RIGHT_OFFER_BUTTON = null;

let _PROPOSAL_INFO_BOX = null;
let _PROPOSAL_INFO_BOX_DESCRIPTION = null;

let _LEFT_PROPOSAL_CONTAINER = null;
let _PROPOSAL_LEFT_SIDE_LIST = null;
let _LEFT_PROPOSAL_BUTTON = null;

let _RIGHT_PROPOSAL_CONTAINER = null;
let _PROPOSAL_RIGHT_NAME_HOLDER = null;
let _PROPOSAL_RIGHT_SIDE_LIST = null;
let _RIGHT_PROPOSAL_BUTTON = null;

let _REPLY_WAITING_SCREEN = null;

export function startOfferProcess(addresseeId, contractorName) {
    HttpUtils.get(`${HttpUtils.baseGameUrl()}/offer/${addresseeId}/info`,
        (preDealInfo) => renderOfferInfoBox(preDealInfo, addresseeId, contractorName));
}

export function renderOfferProposal(offerProposal) {
    renderProposalInfoBox(offerProposal);
}

function createOfferBody() {
    const fieldsToSell = getCheckedFieldValues(OFFER_INITIATOR_CHECKBOX_GROUP_NAME);
    const fieldsToBuy = getCheckedFieldValues(OFFER_ADDRESSEE_CHECKBOX_GROUP_NAME);
    const moneyToGive = _LEFT_MONEY_INPUT.value;
    const moneyToReceive = _RIGHT_MONEY_INPUT.value;
    return new Offer(fieldsToSell, fieldsToBuy, moneyToGive, moneyToReceive);
}

export function renderReplyWaitingScreen() {
    getReplyWaitingScreen().style.display = 'block';
}

export function removeReplyWaitingScreen() {
    getReplyWaitingScreen().style.display = 'none';
}

function renderOfferInfoBox(preDealInfo, addresseeId, contractorName) {
    if (_OFFER_INFO_BOX === null) {
        _OFFER_INFO_BOX = createInfoBox();
    }

    renderOfferInfoBoxDescription();

    renderLeftContainer();
    renderLeftMoneyInput();
    renderLeftPropertyCheckboxes(preDealInfo.offer_initiator_fields);
    renderLeftOfferButton(addresseeId);

    renderRightContainer(contractorName);
    renderRightMoneyInput();
    renderRightPropertyCheckboxes(preDealInfo.offer_addressee_fields);
    renderRightOfferButton();

    _OFFER_INFO_BOX.style.display = 'block';
}

function hideOfferInfoBox() {
    _OFFER_INFO_BOX.style.display = 'none';
}

function renderOfferInfoBoxDescription() {
    if (_OFFER_INFO_BOX_DESCRIPTION === null) {
        _OFFER_INFO_BOX_DESCRIPTION = createInfoBoxDescription(_OFFER_INFO_BOX);
        _OFFER_INFO_BOX_DESCRIPTION.innerText = 'Choose fields to buy or sell and enter money to exchange';
    }
}

function renderLeftContainer() {
    if (_LEFT_SIDE_CONTAINER === null) {
        _LEFT_SIDE_CONTAINER = createOfferSideContainer();
        _LEFT_SIDE_CONTAINER.appendChild(createContainerDescription('You:'));
        _LEFT_SIDE_CONTAINER.style.float = 'left';
        _OFFER_INFO_BOX.appendChild(_LEFT_SIDE_CONTAINER);
    }
}

function renderLeftMoneyInput() {
    if (_LEFT_MONEY_INPUT === null) {
        _LEFT_MONEY_INPUT = createMoneyInput();
        _LEFT_SIDE_CONTAINER.appendChild(_LEFT_MONEY_INPUT);
        bindMoneyInputs();
    }
    _LEFT_MONEY_INPUT.value = '';
}

function renderLeftPropertyCheckboxes(fields) {
    if (_LEFT_CHECKBOXES === null) {
        _LEFT_CHECKBOXES = createSideListContainer();
        _LEFT_SIDE_CONTAINER.appendChild(_LEFT_CHECKBOXES);
    }
    fillCheckboxes(_LEFT_CHECKBOXES, fields, OFFER_INITIATOR_CHECKBOX_GROUP_NAME);
}


function renderLeftOfferButton(addresseeId) {
    if (_LEFT_OFFER_BUTTON === null) {
        _LEFT_OFFER_BUTTON = renderLeftButton(_LEFT_SIDE_CONTAINER, 'Send an offer');
    }
    _LEFT_OFFER_BUTTON.onclick = () => {
        HttpUtils.post(`${HttpUtils.baseGameUrl()}/offer/${addresseeId}/send`, createOfferBody(),
            () => {
                GameMap.hideThrowDiceButton();
                Buttons.removeOldActionContainer();
                hideOfferInfoBox();
            }
        )
    };
}

function renderRightContainer(contractorName) {
    const containerDescription = `${contractorName}:`;
    if (_RIGHT_SIDE_CONTAINER === null) {
        _RIGHT_SIDE_CONTAINER = createOfferSideContainer();
        _RIGHT_SIDE_CONTAINER.style.float = 'right';
        _OFFER_INFO_BOX.appendChild(_RIGHT_SIDE_CONTAINER);
        _RIGHT_CONTAINER_NAME_HOLDER = createContainerDescription(containerDescription);
        _RIGHT_SIDE_CONTAINER.appendChild(_RIGHT_CONTAINER_NAME_HOLDER);
    } else {
        _RIGHT_CONTAINER_NAME_HOLDER.innerText = containerDescription;
    }
}

function renderRightMoneyInput() {
    if (_RIGHT_MONEY_INPUT === null) {
        _RIGHT_MONEY_INPUT = createMoneyInput();
        _RIGHT_SIDE_CONTAINER.appendChild(_RIGHT_MONEY_INPUT);
        bindMoneyInputs();
    }
    _RIGHT_MONEY_INPUT.value = '';
}

function renderRightPropertyCheckboxes(fields) {
    if (_RIGHT_CHECKBOXES === null) {
        _RIGHT_CHECKBOXES = createSideListContainer();
        _RIGHT_SIDE_CONTAINER.appendChild(_RIGHT_CHECKBOXES);
    }
    fillCheckboxes(_RIGHT_CHECKBOXES, fields, OFFER_ADDRESSEE_CHECKBOX_GROUP_NAME);
}

function renderRightOfferButton() {
    if (_RIGHT_OFFER_BUTTON === null) {
        _RIGHT_OFFER_BUTTON = renderRightButton(_RIGHT_SIDE_CONTAINER, 'Cancel');
        _RIGHT_OFFER_BUTTON.onclick = hideOfferInfoBox;
    }
}

function createOfferSideContainer() {
    const offerSideContainer = document.createElement('div');
    offerSideContainer.className = 'offer-side-info-container';
    return offerSideContainer;
}

function createContainerDescription(description) {
    const containerDescription = document.createElement('p');
    containerDescription.className = 'offer-side-name';
    containerDescription.innerText = description;
    return containerDescription;
}

function renderLeftButton(parentOfferBox, name) {
    const leftButton = renderBottomButton(parentOfferBox, name);
    leftButton.style.left = '20%';
    return leftButton;
}

function renderRightButton(parentOfferBox, name) {
    const rightButton = renderBottomButton(parentOfferBox, name);
    rightButton.style.right = '20%';
    return rightButton;
}

function renderBottomButton(parentOfferBox, name) {
    const button = document.createElement('button');
    button.className = 'offer-button';
    button.innerText = name;
    parentOfferBox.appendChild(button);
    return button;
}

function createMoneyInput() {
    const moneyInput = document.createElement('input');
    moneyInput.placeholder = 'sum of money...';
    moneyInput.className = 'offer-money-input';
    return moneyInput;
}

function bindMoneyInputs() {
    if (_LEFT_MONEY_INPUT !== null && _RIGHT_MONEY_INPUT !== null) {
        _LEFT_MONEY_INPUT.oninput = () => {
            _LEFT_MONEY_INPUT.value = _LEFT_MONEY_INPUT.value.replace(/\D/g, '').replace(/^0[^.]/, '0');
            if (_LEFT_MONEY_INPUT.value !== '' && _LEFT_MONEY_INPUT.value !== '0') {
                _RIGHT_MONEY_INPUT.value = '';
            }
        }
        _RIGHT_MONEY_INPUT.oninput = () => {
            _RIGHT_MONEY_INPUT.value = _RIGHT_MONEY_INPUT.value.replace(/\D/g, '').replace(/^0[^.]/, '0');
            if (_RIGHT_MONEY_INPUT.value !== '' && _RIGHT_MONEY_INPUT.value !== '0') {
                _LEFT_MONEY_INPUT.value = '';
            }
        }
    }
}

function createSideListContainer() {
    const sideListContainer = document.createElement('div');
    sideListContainer.className = 'offer-item-list';
    return sideListContainer;
}

function fillCheckboxes(checkboxContainer, fields, groupName) {
    checkboxContainer.innerHTML = '';
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
        checkboxContainer.appendChild(checkboxLine);
    }
}

function renderProposalInfoBox(offerProposal) {
    if (_PROPOSAL_INFO_BOX === null) {
        _PROPOSAL_INFO_BOX = createInfoBox();
    }
    const initiatorName = offerProposal.initiator_name;
    const fieldsToBuy = offerProposal.fields_to_buy;
    const fieldsToSell = offerProposal.fields_to_sell;
    const moneyToGive = offerProposal.money_to_give;
    const moneyToReceive = offerProposal.money_to_receive;

    renderProposalDescription(initiatorName);

    renderLeftProposalContainer(moneyToReceive, fieldsToBuy);
    if (_LEFT_PROPOSAL_BUTTON === null) {
        _LEFT_PROPOSAL_BUTTON = renderLeftButton(_PROPOSAL_INFO_BOX, 'Accept');
        _LEFT_PROPOSAL_BUTTON.onclick = () => {
            HttpUtils.post(`${HttpUtils.baseGameUrl()}/offer/process?action=ACCEPT`);
            hideProposalInfoBox();
        }
    }

    renderRightProposalContainer(initiatorName, moneyToGive, fieldsToSell);
    if (_RIGHT_PROPOSAL_BUTTON === null) {
        _RIGHT_PROPOSAL_BUTTON = renderRightButton(_PROPOSAL_INFO_BOX, 'Decline');
        _RIGHT_PROPOSAL_BUTTON.onclick = () => {
            HttpUtils.post(`${HttpUtils.baseGameUrl()}/offer/process?action=DECLINE`);
            hideProposalInfoBox();
        }
    }

    _PROPOSAL_INFO_BOX.style.display = 'block';
}

function hideProposalInfoBox() {
    _PROPOSAL_INFO_BOX.style.display = 'none';
}

function renderProposalDescription(initiatorName) {
    if (_PROPOSAL_INFO_BOX_DESCRIPTION === null) {
        _PROPOSAL_INFO_BOX_DESCRIPTION = createInfoBoxDescription(_PROPOSAL_INFO_BOX);
    }
    _PROPOSAL_INFO_BOX_DESCRIPTION.innerText = `${initiatorName} made you an offer:`;
}

function renderLeftProposalContainer(moneyToReceive, fieldsToBuy) {
    if (_LEFT_PROPOSAL_CONTAINER === null) {
        _LEFT_PROPOSAL_CONTAINER = createOfferSideContainer();
        _LEFT_PROPOSAL_CONTAINER.appendChild(createContainerDescription('You give:'));
        _LEFT_PROPOSAL_CONTAINER.style.float = 'left';
        _PROPOSAL_INFO_BOX.appendChild(_LEFT_PROPOSAL_CONTAINER);
        _PROPOSAL_LEFT_SIDE_LIST = createSideListContainer();
        _LEFT_PROPOSAL_CONTAINER.appendChild(_PROPOSAL_LEFT_SIDE_LIST);
    }
    fillProposal(_PROPOSAL_LEFT_SIDE_LIST, moneyToReceive, fieldsToBuy);
}

function renderRightProposalContainer(initiatorName, moneyToGive, fieldsToSell) {
    const containerDescription = `${initiatorName} gives:`;
    if (_RIGHT_PROPOSAL_CONTAINER === null) {
        _RIGHT_PROPOSAL_CONTAINER = createOfferSideContainer();
        _RIGHT_PROPOSAL_CONTAINER.style.float = 'right';
        _PROPOSAL_INFO_BOX.appendChild(_RIGHT_PROPOSAL_CONTAINER);
        _PROPOSAL_RIGHT_NAME_HOLDER = createContainerDescription(containerDescription);
        _PROPOSAL_RIGHT_SIDE_LIST = createSideListContainer();
        _RIGHT_PROPOSAL_CONTAINER.append(_PROPOSAL_RIGHT_NAME_HOLDER, _PROPOSAL_RIGHT_SIDE_LIST);
    } else {
        _PROPOSAL_RIGHT_NAME_HOLDER.innerText = containerDescription;
    }
    fillProposal(_PROPOSAL_RIGHT_SIDE_LIST, moneyToGive, fieldsToSell);
}

function fillProposal(sideProposalContainer, money, fields) {
    sideProposalContainer.innerHTML = '';
    const bulletList = document.createElement('ul');
    bulletList.className = 'offer-item-list';
    if (money && money > 0) {
        const moneyProposal = document.createElement('li');
        moneyProposal.innerText = `money: $${money}`;
        bulletList.appendChild(moneyProposal);
    }
    if (fields && fields.length > 0) {
        for (let field of fields) {
            const fieldName = document.createElement('li');
            fieldName.innerText = field.name;
            bulletList.appendChild(fieldName);
        }
    }
    sideProposalContainer.appendChild(bulletList);
}

function getCheckedFieldValues(groupName) {
    const checkedFields = [...document.querySelectorAll(`input[name=${groupName}]:checked`)];
    let fieldsValues;
    if (checkedFields && checkedFields.length > 0) {
        fieldsValues = checkedFields.map(checkbox => checkbox.value);
    }
    return fieldsValues;
}

function getReplyWaitingScreen() {
    if (_REPLY_WAITING_SCREEN === null) {
        _REPLY_WAITING_SCREEN = document.createElement('div');
        _REPLY_WAITING_SCREEN.className = 'offer-reply-waiting-screen';
        document.getElementById('message-container').appendChild(_REPLY_WAITING_SCREEN);

        const waitingMessage = document.createElement('p');
        waitingMessage.innerText = 'Waiting for reply...';
        waitingMessage.className = 'offer-waiting-description';
        const loadingAnimation = document.createElement('div');
        loadingAnimation.className = 'offer-waiting-animation';
        _REPLY_WAITING_SCREEN.append(waitingMessage, loadingAnimation);
    }
    return _REPLY_WAITING_SCREEN;
}

function createInfoBox() {
    const infoBox = document.createElement('div');
    infoBox.className = 'offer-info-box';
    document.getElementById('message-container').appendChild(infoBox);
    return infoBox;
}

function createInfoBoxDescription(infoBox) {
    const infoBoxDescription = document.createElement('span');
    infoBoxDescription.className = 'offer-box-description';
    infoBox.appendChild(infoBoxDescription);
    return infoBoxDescription;
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